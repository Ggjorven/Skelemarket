"use strict";
/* ==========================================================================
   Supermarket Map Designer — editor logic
   One state object (S) backs three views:
     Map    tiles on layers, with per-tile attributes and inventory
     Items  categories and items (unique name, category, sprite)
     Paths  node graph; nodes connect to each other and target map tiles
   ========================================================================== */

/* ---------- Helpers ---------- */
const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];
let uid = 1;
const nid = () => uid++;   // internal numeric ids (sheets, assets, layers, categories, items) — never exported
const esc = s => String(s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
const lower = s => String(s).toLowerCase();
const cap = s => s.charAt(0).toUpperCase() + s.slice(1);
const clamp = (v, lo, hi) => Math.max(lo, Math.min(hi, v));
const basename = p => String(p || '').split('/').pop();
const byName = (a, b) => a.name.localeCompare(b.name);
const sameSrc = (a, b) => !!a && !!b && a[0] === b[0] && a[1] === b[1] && a[2] === b[2] && a[3] === b[3];
const plural = (n, one, many) => `${n} ${n === 1 ? one : many}`;
const readFile = f => new Promise(res => { const r = new FileReader(); r.onload = () => res(r.result); r.readAsDataURL(f); });

/* ---------- State ---------- */
const S = {
  view: 'map',
  mapW: 1280, mapH: 720, grid: 32, snap: true, showGrid: true, dimTiles: true,
  zoom: 1, panX: 60, panY: 60, needFit: false,
  pathPrefix: 'assets/tiles/',

  // Image library — NOT in undo history (holds Image objects)
  sheets: [],      // {id, name, filename, img, dataURL, w, h}
  assets: [],      // tile palette: {id, name, filename, img, dataURL, w, h, pw, ph, source:[x,y,w,h]}

  // Document — in undo history
  layers: [],      // {id, name, visible, locked}
  tiles: [],       // {id:'t1', assetId, layerId, x, y, w, h, attrs:[string], inv:[{itemId, count}]}
  categories: [],  // {id, name}
  items: [],       // {id, name, categoryId, filename, source:[x,y,w,h]}
  nodes: [],       // {id:'n1', x, y, targets:['t1', ...]}
  edges: [],       // [['n1','n2'], ...] — undirected
  activeLayer: null,
  sel: [],         // selected tile ids (Map view)
  nsel: [],        // selected node ids, in the order they were selected (Paths view)

  // UI only
  selAsset: null, selItem: null, itemFilter: 'all', itemSearch: '',
  tool: 'select', ptool: 'select', linkFrom: null, mouse: { x: 0, y: 0 },
  seq: { t: 1, n: 1 },
  history: [], future: [], editing: false,
};
const MAX_HISTORY = 120;
const spriteCache = new Map();   // 'file.png|x,y,w,h' -> dataURL (item thumbnails)

/* ---------- Lookups ---------- */
const assetById = id => S.assets.find(a => a.id === id);
const sheetByFile = f => S.sheets.find(s => s.filename === f);
const tileById = id => S.tiles.find(t => t.id === id);
const layerById = id => S.layers.find(l => l.id === id);
const catById = id => S.categories.find(c => c.id === id);
const catByName = n => S.categories.find(c => lower(c.name) === lower(n));
const itemById = id => S.items.find(i => i.id === id);
const itemByName = n => S.items.find(i => lower(i.name) === lower(n));
const nodeById = id => S.nodes.find(n => n.id === id);
const tileAttrs = t => t.attrs || (t.attrs = []);
const tileInv = t => t.inv || (t.inv = []);
const selectableLayer = id => { const l = layerById(id); return !!(l && l.visible && !l.locked); };
const isSel = id => S.sel.includes(id);
const selTiles = () => S.tiles.filter(t => isSel(t.id));
const selNodes = () => S.nsel.map(nodeById).filter(Boolean);
const inside = (t, x, y) => x >= t.x && x <= t.x + t.w && y >= t.y && y <= t.y + t.h;

function renderList() {   // tiles in draw order: bottom layer first, visible layers only
  const out = [];
  for (const L of S.layers) if (L.visible) for (const t of S.tiles) if (t.layerId === L.id) out.push(t);
  return out;
}
function newId(prefix, exists) { let id; do { id = prefix + S.seq[prefix]++; } while (exists(id)); return id; }
const newTileId = () => newId('t', tileById);
const newNodeId = () => newId('n', nodeById);

/* ---------- Graph helpers ---------- */
const hasEdge = (a, b) => S.edges.some(([x, y]) => (x === a && y === b) || (x === b && y === a));
function addEdge(a, b) { if (a !== b && !hasEdge(a, b)) S.edges.push([a, b]); }
function removeEdge(a, b) { S.edges = S.edges.filter(([x, y]) => !((x === a && y === b) || (x === b && y === a))); }
function toggleEdge(a, b) { if (hasEdge(a, b)) removeEdge(a, b); else addEdge(a, b); }
const neighbors = id => S.edges.filter(e => e.includes(id)).map(([x, y]) => (x === id ? y : x));
function toggleTarget(nodeId, tileId) {
  const n = nodeById(nodeId); if (!n) return;
  const i = n.targets.indexOf(tileId);
  if (i >= 0) n.targets.splice(i, 1); else n.targets.push(tileId);
}

/** Drop every reference that points at something that no longer exists. */
function purgeRefs() {
  const tileIds = new Set(S.tiles.map(t => t.id));
  const nodeIds = new Set(S.nodes.map(n => n.id));
  const itemIds = new Set(S.items.map(i => i.id));
  for (const n of S.nodes) n.targets = n.targets.filter(id => tileIds.has(id));
  S.edges = S.edges.filter(([a, b]) => nodeIds.has(a) && nodeIds.has(b));
  for (const t of S.tiles) t.inv = tileInv(t).filter(e => itemIds.has(e.itemId));
  S.sel = S.sel.filter(id => tileIds.has(id));
  S.nsel = S.nsel.filter(id => nodeIds.has(id));
  if (S.linkFrom && !nodeIds.has(S.linkFrom)) S.linkFrom = null;
  if (S.selItem && !itemIds.has(S.selItem)) S.selItem = null;
}

/* ---------- Bounds ---------- */
const clampX = (x, w) => clamp(x, 0, Math.max(0, S.mapW - w));
const clampY = (y, h) => clamp(y, 0, Math.max(0, S.mapH - h));
function clampTile(t) { t.x = clampX(t.x, t.w); t.y = clampY(t.y, t.h); }
function clampNode(n) { n.x = clamp(n.x, 0, S.mapW); n.y = clamp(n.y, 0, S.mapH); }
/** Limit a group move so no object leaves the map; objects keep their relative offsets. */
function clampDelta(objs, dx, dy) {
  let minDx = -Infinity, maxDx = Infinity, minDy = -Infinity, maxDy = Infinity;
  for (const o of objs) {
    const w = o.w || 0, h = o.h || 0;
    minDx = Math.max(minDx, -o.x); maxDx = Math.min(maxDx, Math.max(0, S.mapW - w) - o.x);
    minDy = Math.max(minDy, -o.y); maxDy = Math.min(maxDy, Math.max(0, S.mapH - h) - o.y);
  }
  return { dx: clamp(dx, minDx, maxDx), dy: clamp(dy, minDy, maxDy) };
}

/* ---------- Undo / redo ---------- */
function snapshot() {
  return JSON.stringify({
    tiles: S.tiles, layers: S.layers, categories: S.categories, items: S.items,
    nodes: S.nodes, edges: S.edges, sel: S.sel, nsel: S.nsel,
    activeLayer: S.activeLayer, mapW: S.mapW, mapH: S.mapH,
  });
}
function pushHistory() {
  S.history.push(snapshot());
  if (S.history.length > MAX_HISTORY) S.history.shift();
  S.future.length = 0;
  updateUndoUI();
}
/** Used by drags: the snapshot taken at mousedown is only committed once something actually changes. */
function ensurePushed() {
  if (!drag || drag.pushed) return;
  S.history.push(drag.preSnap);
  if (S.history.length > MAX_HISTORY) S.history.shift();
  S.future.length = 0; drag.pushed = true; updateUndoUI();
}
/** One history entry per editing session of an input (reset on focusout / key release). */
function beginMutation() { if (!S.editing) { pushHistory(); S.editing = true; } }
function restore(str) {
  const d = JSON.parse(str);
  Object.assign(S, {
    tiles: d.tiles, layers: d.layers, categories: d.categories, items: d.items,
    nodes: d.nodes, edges: d.edges, mapW: d.mapW, mapH: d.mapH, activeLayer: d.activeLayer,
    sel: d.sel || [], nsel: d.nsel || [],
  });
  if (!layerById(S.activeLayer)) S.activeLayer = S.layers[0] && S.layers[0].id;
  purgeRefs();
  $('#mapW').value = S.mapW; $('#mapH').value = S.mapH;
  refreshAll();
}
function undo() { if (!S.history.length) return; S.future.push(snapshot()); restore(S.history.pop()); }
function redo() { if (!S.future.length) return; S.history.push(snapshot()); restore(S.future.pop()); }
function updateUndoUI() { $('#undoBtn').disabled = !S.history.length; $('#redoBtn').disabled = !S.future.length; }
document.addEventListener('focusout', e => { if (e.target && e.target.tagName === 'INPUT') S.editing = false; });

/* ---------- Canvas ---------- */
const canvas = $('#stage'), ctx = canvas.getContext('2d'), workspace = $('#workspace');
let DPR = window.devicePixelRatio || 1;
function resizeCanvas() {
  const r = workspace.getBoundingClientRect();
  DPR = window.devicePixelRatio || 1;
  canvas.width = Math.round(r.width * DPR); canvas.height = Math.round(r.height * DPR);
  canvas.style.width = r.width + 'px'; canvas.style.height = r.height + 'px';
  if (S.needFit) fitView(); else draw();
}
new ResizeObserver(resizeCanvas).observe(workspace);

const toMap = (px, py) => ({ x: (px - S.panX) / S.zoom, y: (py - S.panY) / S.zoom });
const snap = v => (S.snap ? Math.round(v / S.grid) * S.grid : Math.round(v));             // grid lines (tiles)
const snapNode = v => (S.snap ? Math.floor(v / S.grid) * S.grid + S.grid / 2 : Math.round(v)); // cell centres (nodes)
const HANDLE = 8;
const HANDLE_IDS = ['nw', 'n', 'ne', 'e', 'se', 's', 'sw', 'w'];
function handlePoints(t) {
  const { x, y, w, h } = t;
  return [[x, y], [x + w / 2, y], [x + w, y], [x + w, y + h / 2], [x + w, y + h], [x + w / 2, y + h], [x, y + h], [x, y + h / 2]];
}

/* ---------- Rendering ---------- */
let rafPending = false;
function draw() { if (rafPending) return; rafPending = true; requestAnimationFrame(render); }
function render() {
  rafPending = false;
  const z = S.zoom;
  ctx.setTransform(1, 0, 0, 1, 0, 0); ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.setTransform(DPR, 0, 0, DPR, 0, 0);
  ctx.save(); ctx.translate(S.panX, S.panY); ctx.scale(z, z);

  ctx.fillStyle = '#20242b'; ctx.fillRect(-2, -2, S.mapW + 4, S.mapH + 4);
  ctx.fillStyle = '#ffffff'; ctx.fillRect(0, 0, S.mapW, S.mapH);
  if (S.showGrid && S.grid * z > 3) {
    ctx.lineWidth = 1 / z; ctx.strokeStyle = 'rgba(0,0,0,0.08)'; ctx.beginPath();
    for (let x = 0; x <= S.mapW; x += S.grid) { ctx.moveTo(x, 0); ctx.lineTo(x, S.mapH); }
    for (let y = 0; y <= S.mapH; y += S.grid) { ctx.moveTo(0, y); ctx.lineTo(S.mapW, y); }
    ctx.stroke();
  }

  const paths = S.view === 'paths';
  ctx.globalAlpha = paths && S.dimTiles ? 0.4 : 1;
  ctx.imageSmoothingEnabled = false;
  for (const t of renderList()) drawTile(t);
  ctx.globalAlpha = 1;

  ctx.strokeStyle = 'rgba(90,169,255,0.5)'; ctx.lineWidth = 1 / z; ctx.strokeRect(0, 0, S.mapW, S.mapH);
  if (paths) drawPaths(); else drawMapOverlay();
  drawMarquee();
  ctx.restore();
}
function drawTile(t) {
  const a = assetById(t.assetId), z = S.zoom;
  if (a && a.img && a.img.complete && a.img.naturalWidth) { ctx.drawImage(a.img, t.x, t.y, t.w, t.h); return; }
  ctx.fillStyle = 'rgba(242,184,75,0.18)'; ctx.fillRect(t.x, t.y, t.w, t.h);
  ctx.strokeStyle = 'rgba(242,184,75,0.7)'; ctx.lineWidth = 1 / z;
  ctx.strokeRect(t.x + 0.5 / z, t.y + 0.5 / z, t.w - 1 / z, t.h - 1 / z);
}
function dot(x, y, r, color) {
  ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI * 2);
  ctx.fillStyle = color; ctx.fill();
  ctx.lineWidth = 1 / S.zoom; ctx.strokeStyle = 'rgba(0,0,0,0.35)'; ctx.stroke();
}
function drawMapOverlay() {
  const z = S.zoom, r = 3.2 / z, pad = 6 / z;
  // Metadata markers: green = attributes, amber = inventory
  for (const t of renderList()) {
    if (t.w * z < 14) continue;
    let cx = t.x + t.w - pad;
    if (t.attrs && t.attrs.length) { dot(cx, t.y + pad, r, '#3ecf8e'); cx -= r * 2.8; }
    if (t.inv && t.inv.length) dot(cx, t.y + pad, r, '#f2b84b');
  }
  ctx.strokeStyle = '#5aa9ff'; ctx.lineWidth = 1.5 / z;
  for (const t of selTiles()) ctx.strokeRect(t.x, t.y, t.w, t.h);
  if (S.sel.length === 1) {
    const t = tileById(S.sel[0]);
    if (t) { const hs = HANDLE / z; ctx.fillStyle = '#5aa9ff'; for (const [hx, hy] of handlePoints(t)) ctx.fillRect(hx - hs / 2, hy - hs / 2, hs, hs); }
  }
}
function drawPaths() {
  const z = S.zoom, r = 6 / z;
  const visible = t => { const L = layerById(t.layerId); return !!(L && L.visible); };

  // Node → tile target links (dashed amber) and targeted tile outlines
  const targeted = new Set();
  ctx.setLineDash([5 / z, 4 / z]); ctx.lineWidth = 1.5 / z; ctx.strokeStyle = 'rgba(214,150,30,0.95)';
  for (const n of S.nodes) for (const id of n.targets) {
    const t = tileById(id); if (!t || !visible(t)) continue;
    targeted.add(t);
    ctx.beginPath(); ctx.moveTo(n.x, n.y); ctx.lineTo(t.x + t.w / 2, t.y + t.h / 2); ctx.stroke();
  }
  ctx.setLineDash([]);
  ctx.strokeStyle = 'rgba(214,150,30,0.85)'; ctx.lineWidth = 1.5 / z;
  for (const t of targeted) ctx.strokeRect(t.x, t.y, t.w, t.h);

  // Edges
  ctx.strokeStyle = '#1f9d6a'; ctx.lineWidth = 3 / z; ctx.lineCap = 'round'; ctx.beginPath();
  for (const [a, b] of S.edges) { const A = nodeById(a), B = nodeById(b); if (A && B) { ctx.moveTo(A.x, A.y); ctx.lineTo(B.x, B.y); } }
  ctx.stroke(); ctx.lineCap = 'butt';

  // Link tool: hover highlight + rubber band from the active node
  if (S.ptool === 'link') {
    const m = S.mouse, from = nodeById(S.linkFrom), hn = nodeAt(m.x, m.y), ht = hn ? null : tileAtAny(m.x, m.y);
    if (ht) { ctx.strokeStyle = '#f2b84b'; ctx.lineWidth = 2 / z; ctx.strokeRect(ht.x, ht.y, ht.w, ht.h); }
    if (from) {
      ctx.setLineDash([6 / z, 4 / z]); ctx.strokeStyle = 'rgba(90,169,255,0.9)'; ctx.lineWidth = 2 / z;
      ctx.beginPath(); ctx.moveTo(from.x, from.y); ctx.lineTo(hn ? hn.x : m.x, hn ? hn.y : m.y); ctx.stroke();
      ctx.setLineDash([]);
    }
  }

  // Nodes
  const selSet = new Set(S.nsel);
  for (const n of S.nodes) {
    if (n.id === S.linkFrom) { ctx.beginPath(); ctx.arc(n.x, n.y, r * 1.9, 0, Math.PI * 2); ctx.fillStyle = 'rgba(242,184,75,0.4)'; ctx.fill(); }
    ctx.beginPath(); ctx.arc(n.x, n.y, r, 0, Math.PI * 2);
    ctx.fillStyle = selSet.has(n.id) ? '#5aa9ff' : '#3ecf8e'; ctx.fill();
    ctx.lineWidth = 2 / z; ctx.strokeStyle = '#ffffff'; ctx.stroke();
  }
  if (z >= 0.5) {
    ctx.font = `${10 / z}px ui-monospace, Menlo, monospace`; ctx.textBaseline = 'bottom';
    ctx.lineWidth = 3 / z; ctx.strokeStyle = 'rgba(255,255,255,0.9)'; ctx.fillStyle = '#1b2027';
    for (const n of S.nodes) { const x = n.x + r + 2 / z, y = n.y - r * 0.6; ctx.strokeText(n.id, x, y); ctx.fillText(n.id, x, y); }
  }
}
function drawMarquee() {
  if (!drag || !drag.moved || (drag.mode !== 'marquee' && drag.mode !== 'nmarquee')) return;
  const x1 = Math.min(drag.sx, drag.cx), y1 = Math.min(drag.sy, drag.cy), x2 = Math.max(drag.sx, drag.cx), y2 = Math.max(drag.sy, drag.cy);
  ctx.fillStyle = 'rgba(90,169,255,0.12)'; ctx.fillRect(x1, y1, x2 - x1, y2 - y1);
  ctx.strokeStyle = 'rgba(90,169,255,0.9)'; ctx.lineWidth = 1 / S.zoom; ctx.strokeRect(x1, y1, x2 - x1, y2 - y1);
}

/* ---------- Hit testing ---------- */
function topTileAt(mx, my) {   // selectable (visible + unlocked) tiles only
  const list = renderList();
  for (let i = list.length - 1; i >= 0; i--) { const t = list[i]; if (selectableLayer(t.layerId) && inside(t, mx, my)) return t; }
  return null;
}
function tileAtAny(mx, my) {   // any visible tile (link targets can live on locked layers)
  const list = renderList();
  for (let i = list.length - 1; i >= 0; i--) if (inside(list[i], mx, my)) return list[i];
  return null;
}
function nodeAt(mx, my) {
  const rr = (9 / S.zoom) ** 2;
  for (let i = S.nodes.length - 1; i >= 0; i--) { const n = S.nodes[i]; if ((mx - n.x) ** 2 + (my - n.y) ** 2 <= rr) return n; }
  return null;
}
function handleAt(mx, my, t) {
  const tol = (HANDLE / S.zoom) * 0.85, pts = handlePoints(t);
  for (let i = 0; i < pts.length; i++) if (Math.abs(mx - pts[i][0]) <= tol && Math.abs(my - pts[i][1]) <= tol) return HANDLE_IDS[i];
  return null;
}

/* ---------- Selection ---------- */
function setSel(ids) { S.sel = [...new Set(ids)].filter(tileById); syncInspector(); draw(); }
function toggleSel(id) { if (!tileById(id)) return; S.sel = isSel(id) ? S.sel.filter(x => x !== id) : [...S.sel, id]; syncInspector(); draw(); }
function clearSel() { S.sel = []; syncInspector(); draw(); }
function setNSel(ids) { S.nsel = [...new Set(ids)].filter(nodeById); syncInspector(); draw(); }
function toggleNSel(id) { S.nsel = S.nsel.includes(id) ? S.nsel.filter(x => x !== id) : [...S.nsel, id]; syncInspector(); draw(); }

/* ---------- Canvas interaction ---------- */
let drag = null, spaceDown = false;
const beginDrag = extra => Object.assign({ preSnap: snapshot(), pushed: false }, extra);

canvas.addEventListener('contextmenu', e => e.preventDefault());
canvas.addEventListener('mousedown', e => {
  canvas.focus(); S.editing = false;
  const r = canvas.getBoundingClientRect(), px = e.clientX - r.left, py = e.clientY - r.top, m = toMap(px, py);
  if (e.button === 1 || spaceDown) { drag = { mode: 'pan', sx: px, sy: py, px0: S.panX, py0: S.panY }; canvas.style.cursor = 'grabbing'; e.preventDefault(); return; }
  if (e.button !== 0) return;
  if (S.view === 'paths') pathsDown(e, m); else mapDown(e, m);
});

function mapDown(e, m) {
  if (S.tool === 'place') { placeTile(m.x, m.y); return; }
  if (S.tool === 'paint') { drag = beginDrag({ mode: 'paint', last: null }); paintTile(m.x, m.y); return; }
  if (S.sel.length === 1) {
    const t = tileById(S.sel[0]), h = t && handleAt(m.x, m.y, t);
    if (h) { drag = beginDrag({ mode: 'resize', h, sx: t.x, sy: t.y, ex: t.x + t.w, ey: t.y + t.h }); return; }
  }
  const hit = topTileAt(m.x, m.y);
  if (e.shiftKey) {
    if (hit) toggleSel(hit.id);
    else drag = beginDrag({ mode: 'marquee', sx: m.x, sy: m.y, cx: m.x, cy: m.y, moved: false, additive: true });
    return;
  }
  if (hit) {
    if (!isSel(hit.id)) setSel([hit.id]);
    drag = beginDrag({ mode: 'move', startX: m.x, startY: m.y, primary: hit.id, orig: selTiles().map(t => ({ id: t.id, x: t.x, y: t.y, w: t.w, h: t.h })) });
  } else {
    drag = beginDrag({ mode: 'marquee', sx: m.x, sy: m.y, cx: m.x, cy: m.y, moved: false, additive: false });
  }
}

function pathsDown(e, m) {
  const hitN = nodeAt(m.x, m.y);
  if (S.ptool === 'link') { linkClick(hitN, m); return; }
  if (S.ptool === 'node') {
    if (!hitN) { addNodeAt(m.x, m.y, e.shiftKey); return; }
    if (e.shiftKey && S.nsel.length === 1 && S.nsel[0] !== hitN.id) {
      pushHistory(); toggleEdge(S.nsel[0], hitN.id); setNSel([hitN.id]); refreshCount(); return;
    }
  }
  if (hitN) {
    if (e.shiftKey && S.ptool === 'select') { toggleNSel(hitN.id); return; }
    if (!S.nsel.includes(hitN.id)) setNSel([hitN.id]);
    drag = beginDrag({ mode: 'nmove', startX: m.x, startY: m.y, primary: hitN.id, orig: selNodes().map(n => ({ id: n.id, x: n.x, y: n.y, w: 0, h: 0 })) });
    return;
  }
  drag = beginDrag({ mode: 'nmarquee', sx: m.x, sy: m.y, cx: m.x, cy: m.y, moved: false, additive: e.shiftKey });
}

function addNodeAt(x, y, chain) {
  pushHistory();
  const prev = chain && S.nsel.length === 1 ? S.nsel[0] : null;
  const n = { id: newNodeId(), x: clamp(snapNode(x), 0, S.mapW), y: clamp(snapNode(y), 0, S.mapH), targets: [] };
  S.nodes.push(n);
  if (prev) addEdge(prev, n.id);
  setNSel([n.id]); refreshCount();
}

function linkClick(hitN, m) {
  if (hitN) {
    if (S.linkFrom === hitN.id) { S.linkFrom = null; draw(); return; }
    if (!S.linkFrom) { S.linkFrom = hitN.id; setNSel([hitN.id]); return; }
    pushHistory(); toggleEdge(S.linkFrom, hitN.id);
    S.linkFrom = hitN.id;          // chain: keep linking from the node just clicked
    setNSel([hitN.id]); refreshCount(); return;
  }
  const t = tileAtAny(m.x, m.y);
  if (t) {
    if (!S.linkFrom) { toast('Click a node first, then the object it should target.'); return; }
    pushHistory(); toggleTarget(S.linkFrom, t.id); syncInspector(); refreshCount(); draw(); return;
  }
  S.linkFrom = null; draw();
}

window.addEventListener('mousemove', e => {
  if (S.view === 'items') return;
  const r = canvas.getBoundingClientRect(), px = e.clientX - r.left, py = e.clientY - r.top, m = toMap(px, py);
  S.mouse = m;
  $('#stCursor').textContent = `${Math.round(m.x)}, ${Math.round(m.y)}`;
  if (!drag) { updateCursor(m); if (S.view === 'paths' && S.ptool === 'link') draw(); return; }
  switch (drag.mode) {
    case 'pan': S.panX = drag.px0 + (px - drag.sx); S.panY = drag.py0 + (py - drag.sy); draw(); break;
    case 'move': case 'nmove': dragMove(m); break;
    case 'resize': { const t = tileById(S.sel[0]); if (t) { ensurePushed(); doResize(t, drag.h, m); syncTransformFields(); draw(); } break; }
    case 'paint': paintTile(m.x, m.y); break;
    case 'marquee': case 'nmarquee':
      drag.cx = m.x; drag.cy = m.y;
      if (Math.abs(m.x - drag.sx) > 2 || Math.abs(m.y - drag.sy) > 2) drag.moved = true;
      draw(); break;
  }
});

window.addEventListener('mouseup', () => {
  if (!drag) return;
  if (drag.mode === 'resize') {
    const t = tileById(S.sel[0]);
    if (t) { t.w = Math.max(1, Math.round(t.w)); t.h = Math.max(1, Math.round(t.h)); clampTile(t); syncTransformFields(); }
  }
  if (drag.mode === 'marquee' || drag.mode === 'nmarquee') {
    const x1 = Math.min(drag.sx, drag.cx), y1 = Math.min(drag.sy, drag.cy), x2 = Math.max(drag.sx, drag.cx), y2 = Math.max(drag.sy, drag.cy);
    if (drag.mode === 'marquee') {
      if (drag.moved) {
        const picked = renderList().filter(t => selectableLayer(t.layerId) && !(t.x > x2 || t.x + t.w < x1 || t.y > y2 || t.y + t.h < y1)).map(t => t.id);
        setSel(drag.additive ? [...S.sel, ...picked] : picked);
      } else if (!drag.additive) clearSel();
    } else {
      if (drag.moved) {
        const picked = S.nodes.filter(n => n.x >= x1 && n.x <= x2 && n.y >= y1 && n.y <= y2).map(n => n.id);
        setNSel(drag.additive ? [...S.nsel, ...picked] : picked);
      } else if (!drag.additive) setNSel([]);
    }
  }
  drag = null; updateCursorReset(); draw();
});

function dragMove(m) {
  const node = drag.mode === 'nmove', sn = node ? snapNode : snap;
  const po = drag.orig.find(o => o.id === drag.primary); if (!po) return;
  let dx = sn(po.x + (m.x - drag.startX)) - po.x, dy = sn(po.y + (m.y - drag.startY)) - po.y;
  ({ dx, dy } = clampDelta(drag.orig, dx, dy));
  if (dx || dy) ensurePushed();
  for (const o of drag.orig) { const obj = node ? nodeById(o.id) : tileById(o.id); if (obj) { obj.x = o.x + dx; obj.y = o.y + dy; } }
  syncTransformFields(); draw();
}

function doResize(t, h, m) {
  let x1 = drag.sx, y1 = drag.sy, x2 = drag.ex, y2 = drag.ey;
  const L = h.includes('w'), R = h.includes('e'), T = h.includes('n'), B = h.includes('s');
  if (L) x1 = snap(m.x); if (R) x2 = snap(m.x); if (T) y1 = snap(m.y); if (B) y2 = snap(m.y);
  x1 = Math.max(0, x1); y1 = Math.max(0, y1); x2 = Math.min(S.mapW, x2); y2 = Math.min(S.mapH, y2);
  const MIN = S.snap ? S.grid : 2;
  if (x2 - x1 < MIN) { if (L) x1 = Math.max(0, x2 - MIN); else x2 = Math.min(S.mapW, x1 + MIN); }
  if (y2 - y1 < MIN) { if (T) y1 = Math.max(0, y2 - MIN); else y2 = Math.min(S.mapH, y1 + MIN); }
  t.x = x1; t.y = y1; t.w = x2 - x1; t.h = y2 - y1;
}

const HANDLE_CURSORS = { n: 'ns-resize', s: 'ns-resize', e: 'ew-resize', w: 'ew-resize', nw: 'nwse-resize', se: 'nwse-resize', ne: 'nesw-resize', sw: 'nesw-resize' };
function updateCursor(m) {
  let c = 'default';
  if (S.view === 'paths') {
    if (S.ptool === 'link') c = (nodeAt(m.x, m.y) || tileAtAny(m.x, m.y)) ? 'pointer' : 'crosshair';
    else if (nodeAt(m.x, m.y)) c = 'move';
    else if (S.ptool === 'node') c = 'crosshair';
  } else if (S.tool === 'select') {
    if (S.sel.length === 1) { const t = tileById(S.sel[0]), h = t && handleAt(m.x, m.y, t); if (h) c = HANDLE_CURSORS[h]; }
    if (c === 'default' && topTileAt(m.x, m.y)) c = 'move';
  } else c = S.selAsset ? 'crosshair' : 'not-allowed';
  canvas.style.cursor = spaceDown ? 'grab' : c;
}
function updateCursorReset() { canvas.style.cursor = spaceDown ? 'grab' : 'default'; }

/* ---------- Place / paint tiles ---------- */
function activeLayerOK() {
  const L = layerById(S.activeLayer);
  if (!L) return false;
  if (!L.visible) { toast('The active layer is hidden.'); return false; }
  if (L.locked) { toast('The active layer is locked.'); return false; }
  return true;
}
function placeTile(mx, my) {
  const a = assetById(S.selAsset);
  if (!a) { toast('Pick a tile from the palette first.'); return; }
  if (!activeLayerOK()) return;
  pushHistory();
  const w = a.pw, h = a.ph;
  const t = { id: newTileId(), assetId: a.id, layerId: S.activeLayer, x: clampX(snap(mx - w / 2), w), y: clampY(snap(my - h / 2), h), w, h, attrs: [], inv: [] };
  S.tiles.push(t); setSel([t.id]); refreshCount(); renderLayers();
}
function paintTile(mx, my) {
  const a = assetById(S.selAsset); if (!a) return;
  const L = layerById(S.activeLayer); if (!L || !L.visible || L.locked) return;
  const w = a.pw, h = a.ph;
  const gx = clampX(S.snap ? Math.floor(mx / S.grid) * S.grid : Math.round(mx - w / 2), w);
  const gy = clampY(S.snap ? Math.floor(my / S.grid) * S.grid : Math.round(my - h / 2), h);
  const key = `${a.id}:${gx}:${gy}`;
  if (drag && drag.last === key) return;
  if (drag) drag.last = key;
  if (S.tiles.some(t => t.layerId === S.activeLayer && t.assetId === a.id && t.x === gx && t.y === gy)) return;
  ensurePushed();
  S.tiles.push({ id: newTileId(), assetId: a.id, layerId: S.activeLayer, x: gx, y: gy, w, h, attrs: [], inv: [] });
  refreshCount(); renderLayers(); draw();
}

/* ---------- Image library (sheets + tile palette) ---------- */
function cropDataURL(img, sx, sy, sw, sh) {
  const c = document.createElement('canvas'); c.width = Math.max(1, sw); c.height = Math.max(1, sh);
  const x = c.getContext('2d'); x.imageSmoothingEnabled = false;
  x.drawImage(img, sx, sy, sw, sh, 0, 0, sw, sh);
  return c.toDataURL('image/png');
}
function makeImg(dataURL) { const img = new Image(); img.onload = () => draw(); img.src = dataURL; return img; }
function addAssetObj(o) { const a = Object.assign({ id: nid(), pw: o.w, ph: o.h }, o); S.assets.push(a); return a; }
function addSpriteAsset(sheet, sx, sy, sw, sh, name) {
  const dataURL = cropDataURL(sheet.img, sx, sy, sw, sh);
  return addAssetObj({ name: name || `${sheet.name}_${sx}_${sy}`, filename: sheet.filename, img: makeImg(dataURL), dataURL, w: sw, h: sh, source: [sx, sy, sw, sh] });
}
/** Add (or replace) a sheet. Anything that references its file name — placeholders included — is re-cropped. */
function addSheet(dataURL, filename) {
  return new Promise(res => {
    const img = new Image();
    img.onload = () => {
      let sh = sheetByFile(filename);
      if (sh) Object.assign(sh, { img, dataURL, w: img.naturalWidth, h: img.naturalHeight });
      else { sh = { id: nid(), name: filename.replace(/\.[^.]+$/, ''), filename, img, dataURL, w: img.naturalWidth, h: img.naturalHeight }; S.sheets.push(sh); }
      relinkFile(filename);
      res(sh);
    };
    img.src = dataURL;
  });
}
function relinkFile(filename) {
  const sh = sheetByFile(filename); if (!sh) return;
  for (const a of S.assets) {
    if (a.filename !== filename) continue;
    a.dataURL = cropDataURL(sh.img, ...(a.source || [0, 0, sh.w, sh.h]));
    a.img = makeImg(a.dataURL);
  }
  for (const k of [...spriteCache.keys()]) if (k.startsWith(filename + '|')) spriteCache.delete(k);
}
function spriteURL(filename, source) {
  if (!filename || !source) return null;
  const sh = sheetByFile(filename); if (!sh) return null;
  const key = `${filename}|${source.join(',')}`;
  if (!spriteCache.has(key)) spriteCache.set(key, cropDataURL(sh.img, ...source));
  return spriteCache.get(key);
}
const itemSprite = it => spriteURL(it.filename, it.source);

async function uploadTileFiles(files) {
  for (const f of files) {
    if (!/image\/(png|jpeg)/.test(f.type)) continue;
    const sh = await addSheet(await readFile(f), f.name);
    const full = [0, 0, sh.w, sh.h];
    let a = S.assets.find(x => x.filename === sh.filename && sameSrc(x.source, full));
    if (!a) a = addSpriteAsset(sh, 0, 0, sh.w, sh.h, sh.name);
    if (!S.selAsset) S.selAsset = a.id;
  }
  afterLibraryChange();
  if (S.selAsset) selectAsset(S.selAsset);
}
function afterLibraryChange() { renderAssets(); if (S.view === 'items') renderItemsView(); syncInspector(); draw(); }

function renderAssets() {
  const list = $('#assetList'); list.innerHTML = '';
  $('#assetEmpty').classList.toggle('hidden', S.assets.length > 0);
  for (const a of S.assets) {
    const sh = sheetByFile(a.filename);
    const sliced = !!(sh && a.source && !sameSrc(a.source, [0, 0, sh.w, sh.h]));
    const el = document.createElement('div');
    el.className = 'asset' + (a.id === S.selAsset ? ' sel' : '');
    el.innerHTML = `${sliced ? '<span class="badge">SHEET</span>' : ''}<button class="del" title="Delete tile">✕</button>
      <div class="thumb checker">${a.dataURL ? `<img src="${a.dataURL}" alt="">` : '<span class="miss">image not loaded</span>'}</div>
      <div class="nm" title="${esc(a.name)}">${esc(a.name)}</div><div class="sz">${a.w}×${a.h}px</div>`;
    el.querySelector('.del').addEventListener('click', ev => { ev.stopPropagation(); deleteAsset(a.id); });
    el.addEventListener('click', () => selectAsset(a.id));
    list.appendChild(el);
  }
}
function deleteAsset(id) {
  const used = S.tiles.filter(t => t.assetId === id).length;
  if (used && !confirm(`This tile is placed ${plural(used, 'time', 'times')}. Delete it and every placed copy?`)) return;
  pushHistory();
  S.tiles = S.tiles.filter(t => t.assetId !== id);
  S.assets = S.assets.filter(a => a.id !== id);
  if (S.selAsset === id) S.selAsset = null;
  purgeRefs(); renderAssets(); refreshAll();
}
function selectAsset(id) {
  S.selAsset = id; renderAssets();
  const a = assetById(id);
  $('#assetProps').classList.toggle('hidden', !a);
  if (a) {
    $('#aFilename').value = a.filename; $('#aW').value = a.pw; $('#aH').value = a.ph;
    $('#aSource').textContent = a.source ? `[${a.source.map(Math.round).join(', ')}]` : '—';
    if (S.tool === 'select' && S.view === 'map') setTool('place');
  }
  syncInspector(); draw();
}
$('#uploadBtn').addEventListener('click', () => $('#fileInput').click());
$('#fileInput').addEventListener('change', e => { uploadTileFiles([...e.target.files]); e.target.value = ''; });
['dragover', 'drop'].forEach(ev => workspace.addEventListener(ev, e => e.preventDefault()));
workspace.addEventListener('drop', e => { if (S.view === 'map') uploadTileFiles([...e.dataTransfer.files]); });

/* ---------- Layers ---------- */
function addLayer() {
  pushHistory();
  const L = { id: nid(), name: `Layer ${S.layers.length + 1}`, visible: true, locked: false };
  S.layers.push(L); S.activeLayer = L.id; renderLayers(); draw();
}
function deleteLayer(id) {
  if (S.layers.length <= 1) { toast('A map needs at least one layer.'); return; }
  const cnt = S.tiles.filter(t => t.layerId === id).length;
  if (cnt && !confirm(`Delete this layer and its ${plural(cnt, 'tile', 'tiles')}?`)) return;
  pushHistory();
  S.tiles = S.tiles.filter(t => t.layerId !== id);
  S.layers = S.layers.filter(l => l.id !== id);
  if (S.activeLayer === id) S.activeLayer = S.layers[S.layers.length - 1].id;
  purgeRefs(); refreshAll();
}
function moveLayer(id, dir) {
  const i = S.layers.findIndex(l => l.id === id), j = i + dir;
  if (j < 0 || j >= S.layers.length) return;
  pushHistory(); const [L] = S.layers.splice(i, 1); S.layers.splice(j, 0, L); renderLayers(); draw();
}
function toggleLayerFlag(id, flag) {
  const L = layerById(id); pushHistory(); L[flag] = !L[flag];
  S.sel = S.sel.filter(sid => { const t = tileById(sid); return t && selectableLayer(t.layerId); });
  renderLayers(); syncInspector(); draw();
}
function renderLayers() {
  const el = $('#layerList'); el.innerHTML = '';
  [...S.layers].reverse().forEach(L => {
    const cnt = S.tiles.filter(t => t.layerId === L.id).length;
    const row = document.createElement('div');
    row.className = 'layer' + (L.id === S.activeLayer ? ' active' : '') + (L.visible ? '' : ' hiddenL');
    row.innerHTML = `<button class="ic vis" title="Show / hide">${L.visible ? '👁' : '🚫'}</button>
      <button class="ic lock" title="Lock / unlock">${L.locked ? '🔒' : '🔓'}</button>
      <input class="lname" value="${esc(L.name)}" spellcheck="false">
      <span class="cnt">${cnt}</span>
      <span class="ord"><button class="up" title="Move up">▲</button><button class="dn" title="Move down">▼</button></span>
      <button class="ic del" title="Delete layer">✕</button>`;
    row.addEventListener('click', ev => { if (ev.target.closest('button') || ev.target.tagName === 'INPUT') return; S.activeLayer = L.id; renderLayers(); });
    row.querySelector('.vis').addEventListener('click', () => toggleLayerFlag(L.id, 'visible'));
    row.querySelector('.lock').addEventListener('click', () => toggleLayerFlag(L.id, 'locked'));
    row.querySelector('.up').addEventListener('click', () => moveLayer(L.id, 1));
    row.querySelector('.dn').addEventListener('click', () => moveLayer(L.id, -1));
    row.querySelector('.del').addEventListener('click', () => deleteLayer(L.id));
    const inp = row.querySelector('.lname');
    inp.addEventListener('change', () => { pushHistory(); L.name = inp.value.trim() || 'Layer'; renderLayers(); });
    inp.addEventListener('keydown', ev => { if (ev.key === 'Enter') inp.blur(); });
    el.appendChild(row);
  });
  const L = layerById(S.activeLayer);
  $('#stLayer').textContent = L ? L.name : '—';
}
$('#addLayerBtn').addEventListener('click', addLayer);

/* ---------- Inspectors ---------- */
function syncInspector() { syncMapInspector(); syncPathInspector(); }
function syncTransformFields() {
  if (S.sel.length === 1) {
    const t = tileById(S.sel[0]);
    if (t) { $('#tX').value = Math.round(t.x); $('#tY').value = Math.round(t.y); $('#tW').value = Math.round(t.w); $('#tH').value = Math.round(t.h); }
  }
  if (S.nsel.length === 1) {
    const n = nodeById(S.nsel[0]);
    if (n) { $('#pX').value = Math.round(n.x); $('#pY').value = Math.round(n.y); }
  }
}

/* Map inspector */
function syncMapInspector() {
  const n = S.sel.length;
  $('#tileEdit').classList.toggle('hidden', n !== 1);
  $('#attrBlock').classList.toggle('hidden', n < 1);
  $('#invBlock').classList.toggle('hidden', n < 1);
  $('#selActions').classList.toggle('hidden', n < 1);
  $('#noSel').classList.toggle('hidden', n > 0 || !!S.selAsset);
  $('#selCount').textContent = `${plural(n, 'tile', 'tiles')} selected`;
  if (n === 1) {
    const t = tileById(S.sel[0]), a = assetById(t.assetId);
    $('#tId').textContent = t.id;
    $('#tSource').textContent = a && a.source ? `[${a.source.map(Math.round).join(', ')}]  ${a.filename}` : '—';
    const from = S.nodes.filter(nd => nd.targets.includes(t.id)).map(nd => nd.id);
    $('#tLinks').textContent = from.length ? from.join(', ') : '—';
  }
  syncTransformFields();
  if (n >= 1) { renderAttrEditor(); renderInvEditor(); }
}

function renderAttrEditor() {
  const tiles = selTiles(); if (!tiles.length) return;
  const count = {};
  for (const t of tiles) for (const a of tileAttrs(t)) count[a] = (count[a] || 0) + 1;
  const keys = Object.keys(count).sort();

  const chips = $('#attrChips');
  chips.innerHTML = keys.length ? '' : `<span class="attrempty">No attributes on ${tiles.length > 1 ? 'these tiles' : 'this tile'} yet.</span>`;
  for (const k of keys) {
    const all = count[k] === tiles.length;
    const chip = document.createElement('span');
    chip.className = 'chip' + (catByName(k) ? ' cat' : '') + (all ? '' : ' partial');
    if (!all) chip.title = `On ${count[k]} of ${tiles.length} selected`;
    chip.innerHTML = `<span>${esc(k)}</span><button title="Remove">×</button>`;
    chip.querySelector('button').addEventListener('click', () => removeAttr(k));
    chips.appendChild(chip);
  }

  // Category picker: click toggles the category on the whole selection
  const pick = $('#attrCats'); pick.innerHTML = '';
  if (!S.categories.length) pick.innerHTML = '<span class="attrempty">No categories yet. Create them in the Items tab.</span>';
  for (const c of [...S.categories].sort(byName)) {
    const k = count[c.name] || 0, all = k === tiles.length;
    const b = document.createElement('button');
    b.textContent = c.name; b.className = all ? 'on' : k ? 'partial' : '';
    b.title = all ? 'Remove from selection' : 'Add to selection';
    b.addEventListener('click', () => (all ? removeAttr(c.name) : addAttrs([c.name])));
    pick.appendChild(b);
  }
  $('#catDatalist').innerHTML = S.categories.map(c => `<option value="${esc(c.name)}">`).join('');
  $('#attrHint').textContent = tiles.length > 1
    ? `Changes apply to all ${tiles.length} selected tiles. Dashed means only some have it.`
    : 'Type any tag, or toggle one of your item categories.';
}
function addAttrs(tags) {
  if (!tags.length || !S.sel.length) return;
  pushHistory();
  for (const t of selTiles()) { const a = tileAttrs(t); for (const tag of tags) if (!a.includes(tag)) a.push(tag); }
  renderAttrEditor(); draw();
}
function addAttrFromInput() {
  // A typed tag that matches a category (case-insensitive) is stored with the category's exact spelling
  const tags = $('#attrInput').value.split(',').map(s => s.trim()).filter(Boolean).map(s => (catByName(s) ? catByName(s).name : s));
  if (!tags.length) return;
  $('#attrInput').value = '';
  addAttrs(tags);
}
function removeAttr(tag) {
  if (!S.sel.length) return;
  pushHistory();
  for (const t of selTiles()) t.attrs = tileAttrs(t).filter(a => a !== tag);
  renderAttrEditor(); draw();
}
$('#attrAdd').addEventListener('click', addAttrFromInput);
$('#attrInput').addEventListener('keydown', e => { if (e.key === 'Enter') { addAttrFromInput(); e.preventDefault(); } });

function fillItemSelect(sel) {
  const prev = sel.value;
  if (!S.items.length) { sel.innerHTML = '<option value="">No items yet</option>'; sel.disabled = true; return; }
  sel.disabled = false; sel.innerHTML = '';
  const groups = S.categories.slice().sort(byName).map(c => ({ label: c.name, items: S.items.filter(i => i.categoryId === c.id) }));
  groups.push({ label: 'Uncategorized', items: S.items.filter(i => !catById(i.categoryId)) });
  for (const g of groups) {
    if (!g.items.length) continue;
    const og = document.createElement('optgroup'); og.label = g.label;
    for (const it of g.items.slice().sort(byName)) { const o = document.createElement('option'); o.value = it.id; o.textContent = it.name; og.appendChild(o); }
    sel.appendChild(og);
  }
  if (prev && itemById(+prev)) sel.value = prev;
}
function renderInvEditor() {
  const tiles = selTiles(); if (!tiles.length) return;
  const single = tiles.length === 1;
  const agg = new Map();   // itemId -> {k: tiles holding it, total}
  for (const t of tiles) for (const e of tileInv(t)) { const a = agg.get(e.itemId) || { k: 0, total: 0 }; a.k++; a.total += e.count; agg.set(e.itemId, a); }
  const rows = [...agg].map(([id, a]) => ({ it: itemById(id), a })).filter(r => r.it).sort((x, y) => byName(x.it, y.it));

  const list = $('#invList'); list.innerHTML = rows.length ? '' : '<div class="attrempty">Empty.</div>';
  for (const { it, a } of rows) {
    const cat = catById(it.categoryId), url = itemSprite(it);
    const row = document.createElement('div'); row.className = 'invrow';
    row.innerHTML = `<span class="ithumb checker">${url ? `<img src="${url}" alt="">` : ''}</span>
      <span class="nm"><b>${esc(it.name)}</b><small>${cat ? esc(cat.name) : 'Uncategorized'}</small></span>
      ${single ? `<input type="number" min="1" value="${a.total}" title="Count">` : `<span class="qty" title="Total count, tiles holding it">${a.total} in ${a.k}/${tiles.length}</span>`}
      <button class="x" title="${single ? 'Remove' : 'Remove from all selected'}">×</button>`;
    if (single) row.querySelector('input').addEventListener('input', ev => {
      const v = parseInt(ev.target.value, 10); if (!(v >= 1)) return;
      const e = tileInv(tiles[0]).find(x => x.itemId === it.id); if (!e) return;
      beginMutation(); e.count = v;
    });
    row.querySelector('.x').addEventListener('click', () => removeFromInv(it.id));
    list.appendChild(row);
  }
  fillItemSelect($('#invItem'));
  $('#invAdd').disabled = !S.items.length;
  $('#invHint').textContent = !S.items.length ? 'No items yet. Create them in the Items tab.'
    : single ? 'Adding an item that is already here increases its count.' : `Adds to all ${tiles.length} selected tiles.`;
}
function addToInv() {
  const id = +$('#invItem').value, cnt = Math.max(1, parseInt($('#invCount').value, 10) || 1);
  if (!itemById(id) || !S.sel.length) return;
  pushHistory();
  for (const t of selTiles()) { const inv = tileInv(t), e = inv.find(x => x.itemId === id); if (e) e.count += cnt; else inv.push({ itemId: id, count: cnt }); }
  renderInvEditor(); draw();
}
function removeFromInv(id) { pushHistory(); for (const t of selTiles()) t.inv = tileInv(t).filter(e => e.itemId !== id); renderInvEditor(); draw(); }
$('#invAdd').addEventListener('click', addToInv);

function bindNum(sel, apply) {
  $(sel).addEventListener('input', () => { const v = parseFloat($(sel).value); if (isNaN(v)) return; beginMutation(); apply(v); draw(); });
}
bindNum('#tX', v => { const t = tileById(S.sel[0]); if (t) { t.x = v; clampTile(t); } });
bindNum('#tY', v => { const t = tileById(S.sel[0]); if (t) { t.y = v; clampTile(t); } });
bindNum('#tW', v => { const t = tileById(S.sel[0]); if (t) t.w = clamp(v, 1, S.mapW - t.x); });
bindNum('#tH', v => { const t = tileById(S.sel[0]); if (t) t.h = clamp(v, 1, S.mapH - t.y); });
bindNum('#pX', v => { const n = nodeById(S.nsel[0]); if (n) n.x = clamp(v, 0, S.mapW); });
bindNum('#pY', v => { const n = nodeById(S.nsel[0]); if (n) n.y = clamp(v, 0, S.mapH); });
bindNum('#aW', v => { const a = assetById(S.selAsset); if (a) a.pw = Math.max(1, Math.round(v)); });
bindNum('#aH', v => { const a = assetById(S.selAsset); if (a) a.ph = Math.max(1, Math.round(v)); });
$('#aFilename').addEventListener('input', () => {
  const a = assetById(S.selAsset); if (!a) return;
  a.filename = $('#aFilename').value; a.name = a.filename.replace(/\.[^.]+$/, '');
  renderAssets(); syncInspector();
});

$('#toFront').addEventListener('click', () => { if (!S.sel.length) return; pushHistory(); S.tiles = S.tiles.filter(t => !isSel(t.id)).concat(selTiles()); draw(); });
$('#toBack').addEventListener('click', () => { if (!S.sel.length) return; pushHistory(); S.tiles = selTiles().concat(S.tiles.filter(t => !isSel(t.id))); draw(); });
$('#dupBtn').addEventListener('click', dupSel);
$('#delTile').addEventListener('click', delSel);
$('#moveToLayer').addEventListener('click', () => {
  if (!S.sel.length || !activeLayerOK()) return;
  pushHistory(); for (const t of selTiles()) t.layerId = S.activeLayer; renderLayers(); syncInspector(); draw();
});
function dupSel() {
  if (!S.sel.length) return;
  pushHistory();
  const copies = selTiles().map(t => {
    const c = { ...t, id: newTileId(), x: t.x + S.grid, y: t.y + S.grid, attrs: [...tileAttrs(t)], inv: tileInv(t).map(e => ({ ...e })) };
    clampTile(c); return c;
  });
  S.tiles.push(...copies); setSel(copies.map(c => c.id)); refreshCount(); renderLayers();
}
function delSel() {
  if (!S.sel.length) return;
  pushHistory(); const del = new Set(S.sel);
  S.tiles = S.tiles.filter(t => !del.has(t.id));
  purgeRefs(); refreshAll();
}
$('#clearBtn').addEventListener('click', () => {
  if (!S.tiles.length || !confirm('Remove all placed tiles? Palette, layers, items and path nodes are kept; node targets are cleared.')) return;
  pushHistory(); S.tiles = []; purgeRefs(); refreshAll();
});

/* Paths inspector */
function syncPathInspector() {
  const nodes = selNodes(), n = nodes.length;
  $('#pNodeEdit').classList.toggle('hidden', n !== 1);
  $('#pSelActions').classList.toggle('hidden', n < 1);
  $('#pNoSel').classList.toggle('hidden', n > 0);
  $('#pSelCount').textContent = `${plural(n, 'node', 'nodes')} selected`;
  $('#pLinkChain').disabled = n < 2;
  if (n === 1) {
    const nd = nodes[0], nb = neighbors(nd.id);
    $('#pNodeId').textContent = nd.id;
    $('#pEdges').innerHTML = nb.length
      ? nb.map(id => `<span class="chip"><span>${esc(id)}</span><button data-unlink="${esc(id)}" title="Disconnect">×</button></span>`).join('')
      : '<span class="attrempty">Not connected yet.</span>';
    $('#pTargets').innerHTML = nd.targets.length
      ? nd.targets.map(id => {
          const t = tileById(id), a = t && assetById(t.assetId);
          return `<span class="chip tgt" title="${a ? esc(a.name) : ''}"><span>${esc(id)}${a ? ' ' + esc(a.name) : ''}</span><button data-untarget="${esc(id)}" title="Remove target">×</button></span>`;
        }).join('')
      : '<span class="attrempty">No targets. With Link, click this node and then an object.</span>';
  }
  syncTransformFields();
}
$('#pEdges').addEventListener('click', e => {
  const b = e.target.closest('[data-unlink]'), nd = selNodes()[0]; if (!b || !nd) return;
  pushHistory(); removeEdge(nd.id, b.dataset.unlink); syncInspector(); refreshCount(); draw();
});
$('#pTargets').addEventListener('click', e => {
  const b = e.target.closest('[data-untarget]'), nd = selNodes()[0]; if (!b || !nd) return;
  pushHistory(); toggleTarget(nd.id, b.dataset.untarget); syncInspector(); refreshCount(); draw();
});
$('#pLinkChain').addEventListener('click', () => {
  const ns = selNodes(); if (ns.length < 2) return;
  pushHistory(); for (let i = 0; i < ns.length - 1; i++) addEdge(ns[i].id, ns[i + 1].id);
  syncInspector(); refreshCount(); draw();
});
$('#pDelete').addEventListener('click', deleteNodes);
function deleteNodes() {
  if (!S.nsel.length) return;
  pushHistory(); const del = new Set(S.nsel);
  S.nodes = S.nodes.filter(n => !del.has(n.id));
  purgeRefs(); refreshAll();
}
$('#clearPaths').addEventListener('click', () => {
  if (!S.nodes.length || !confirm('Remove all path nodes, edges and targets?')) return;
  pushHistory(); S.nodes = []; S.edges = []; purgeRefs(); refreshAll();
});
$('#dimChk').addEventListener('change', e => { S.dimTiles = e.target.checked; draw(); });

/* ---------- Items view: categories ---------- */
function itemUsage(id) {
  let tiles = 0, units = 0;
  for (const t of S.tiles) { const e = tileInv(t).find(x => x.itemId === id); if (e) { tiles++; units += e.count; } }
  return { tiles, units };
}
function renderItemsView() {
  if (typeof S.itemFilter === 'number' && !catById(S.itemFilter)) S.itemFilter = 'all';
  renderCategories(); renderItemGrid(); renderItemEditor();
}
function renderCategories() {
  const el = $('#catList'); el.innerHTML = '';
  const mk = (filter, inner, count) => {
    const row = document.createElement('div');
    row.className = 'catrow' + (S.itemFilter === filter ? ' active' : '');
    row.innerHTML = `${inner}<span class="cnt">${count}</span>`;
    row.addEventListener('click', e => {
      if (e.target.closest('button') || S.itemFilter === filter) return;
      S.itemFilter = filter;
      $$('.catrow').forEach(r => r.classList.toggle('active', r === row));   // no rebuild: keeps focus in the name field
      renderItemGrid();
    });
    el.appendChild(row);
    return row;
  };
  mk('all', '<span class="label">All items</span>', S.items.length).insertAdjacentHTML('beforeend', '<span class="ic-spacer"></span>');
  for (const c of S.categories.slice().sort(byName)) {
    const row = mk(c.id, `<input class="lname" value="${esc(c.name)}" spellcheck="false" title="Rename">`, S.items.filter(i => i.categoryId === c.id).length);
    const del = document.createElement('button');
    del.className = 'ic'; del.title = 'Delete category'; del.textContent = '✕';
    del.addEventListener('click', () => deleteCategory(c.id));
    row.appendChild(del);
    const inp = row.querySelector('input');
    inp.addEventListener('change', () => renameCategory(c.id, inp.value));
    inp.addEventListener('keydown', e => { if (e.key === 'Enter') inp.blur(); if (e.key === 'Escape') { inp.value = c.name; inp.blur(); } });
  }
  const none = S.items.filter(i => !catById(i.categoryId)).length;
  if (none) mk('none', '<span class="label muted">Uncategorized</span>', none).insertAdjacentHTML('beforeend', '<span class="ic-spacer"></span>');
  $('#catEmpty').classList.toggle('hidden', S.categories.length > 0);
}
function addCategory() {
  const v = $('#catNew').value.trim(); if (!v) return;
  if (catByName(v)) { toast(`Category "${catByName(v).name}" already exists.`); return; }
  pushHistory(); S.categories.push({ id: nid(), name: v });
  $('#catNew').value = ''; renderItemsView();
}
/** Renaming a category also renames it in tile attributes, so shelves keep matching. */
function renameCategory(id, value) {
  const c = catById(id), v = value.trim(); if (!c) return;
  const clash = catByName(v);
  if (!v || (clash && clash.id !== id)) { toast(v ? `Category "${clash.name}" already exists.` : 'A category needs a name.'); renderCategories(); return; }
  if (v === c.name) return;
  pushHistory();
  const old = c.name; c.name = v;
  for (const t of S.tiles) {
    const a = tileAttrs(t), i = a.indexOf(old);
    if (i >= 0) { if (a.includes(v)) a.splice(i, 1); else a[i] = v; }
  }
  renderItemsView();
}
function deleteCategory(id) {
  const c = catById(id); if (!c) return;
  const items = S.items.filter(i => i.categoryId === id).length, tiles = S.tiles.filter(t => tileAttrs(t).includes(c.name)).length;
  if ((items || tiles) && !confirm(`Delete "${c.name}"? ${plural(items, 'item becomes', 'items become')} uncategorized and it is removed from ${plural(tiles, 'tile', 'tiles')}' attributes.`)) return;
  pushHistory();
  S.categories = S.categories.filter(x => x.id !== id);
  for (const it of S.items) if (it.categoryId === id) it.categoryId = null;
  for (const t of S.tiles) t.attrs = tileAttrs(t).filter(a => a !== c.name);
  renderItemsView();
}
$('#catAdd').addEventListener('click', addCategory);
$('#catNew').addEventListener('keydown', e => { if (e.key === 'Enter') { addCategory(); e.preventDefault(); } });

/* ---------- Items view: item grid + editor ---------- */
function renderItemGrid() {
  const f = S.itemFilter, q = lower(S.itemSearch.trim());
  let list = S.items.filter(it => f === 'all' || (f === 'none' ? !catById(it.categoryId) : it.categoryId === f));
  if (q) list = list.filter(it => lower(it.name).includes(q));
  list.sort(byName);
  const c = typeof f === 'number' ? catById(f) : null;
  $('#itemsTitle').textContent = f === 'none' ? 'Uncategorized' : c ? c.name : 'All items';

  const grid = $('#itemGrid'); grid.innerHTML = '';
  for (const it of list) {
    const url = itemSprite(it), cat = catById(it.categoryId), u = itemUsage(it.id);
    const card = document.createElement('div');
    card.className = 'icard' + (it.id === S.selItem ? ' sel' : '');
    card.innerHTML = `<div class="thumb checker">${url ? `<img src="${url}" alt="">` : `<span class="miss">${it.filename ? 'sheet not loaded' : 'no sprite'}</span>`}</div>
      <div class="nm" title="${esc(it.name)}">${esc(it.name)}</div>
      <div class="cat${cat ? '' : ' none'}">${cat ? esc(cat.name) : 'Uncategorized'}</div>
      <div class="use">${u.tiles ? `${u.units} on ${plural(u.tiles, 'tile', 'tiles')}` : 'not stocked'}</div>`;
    card.addEventListener('click', () => selectItem(it.id));
    grid.appendChild(card);
  }
  const empty = $('#itemEmpty');
  empty.classList.toggle('hidden', list.length > 0);
  empty.innerHTML = S.items.length ? 'No items match.' : 'No items yet.<br>Add categories on the left, then click <b>+ New item</b>.';
}
function selectItem(id) { S.selItem = id; setNameErr(''); renderItemGrid(); renderItemEditor(); }
function setNameErr(msg) { $('#iNameErr').textContent = msg; $('#iNameErr').classList.toggle('hidden', !msg); $('#iName').classList.toggle('bad', !!msg); }
function renderItemEditor() {
  const it = itemById(S.selItem);
  $('#iEdit').classList.toggle('hidden', !it); $('#iNoSel').classList.toggle('hidden', !!it);
  if (!it) return;
  if (document.activeElement !== $('#iName')) $('#iName').value = it.name;
  $('#iCat').innerHTML = '<option value="">None</option>' + S.categories.slice().sort(byName).map(c => `<option value="${c.id}">${esc(c.name)}</option>`).join('');
  $('#iCat').value = catById(it.categoryId) ? String(it.categoryId) : '';
  $('#iCatHint').classList.toggle('hidden', S.categories.length > 0);
  const url = itemSprite(it);
  $('#iSprite').innerHTML = url ? `<img src="${url}" alt="">`
    : `<span class="miss">${it.filename ? `Upload ${esc(it.filename)} (Pick from sheet → Upload sheet) to see this sprite.` : 'No sprite yet.'}</span>`;
  $('#iSrc').textContent = it.source ? `[${it.source.join(', ')}]` : '—';
  $('#iPath').textContent = it.filename ? S.pathPrefix + it.filename : '—';
  $('#iFromTile').innerHTML = '<option value="">Reuse a tile sprite…</option>' + S.assets.map(a => `<option value="${a.id}">${esc(a.name)}</option>`).join('');
  $('#iFromTile').disabled = !S.assets.length;
  $('#iClear').disabled = !it.source;
  const u = itemUsage(it.id);
  $('#iUsage').textContent = u.tiles ? `Stocked on ${plural(u.tiles, 'tile', 'tiles')}, ${u.units} units in total.` : 'Not stocked on any tile yet.';
}
function newItem() {
  pushHistory();
  let name = 'NEW_ITEM', i = 2;
  while (itemByName(name)) name = `NEW_ITEM_${i++}`;
  const it = { id: nid(), name, categoryId: typeof S.itemFilter === 'number' ? S.itemFilter : null, filename: null, source: null };
  S.items.push(it); S.itemSearch = ''; $('#itemSearch').value = '';
  selectItem(it.id); renderCategories(); refreshCount();
  const inp = $('#iName'); inp.focus(); inp.select();
}
function onItemNameInput() {
  const it = itemById(S.selItem); if (!it) return;
  const v = $('#iName').value.trim();
  if (!v) { setNameErr('An item needs a name.'); return; }
  const other = itemByName(v);
  if (other && other.id !== it.id) { setNameErr(`${other.name} already exists. Item names must be unique.`); return; }
  setNameErr('');
  if (v === it.name) return;
  beginMutation(); it.name = v; renderItemGrid();
}
function deleteItem() {
  const it = itemById(S.selItem); if (!it) return;
  const u = itemUsage(it.id);
  if (u.tiles && !confirm(`${it.name} is stocked on ${plural(u.tiles, 'tile', 'tiles')}. Delete it and remove it from those inventories?`)) return;
  pushHistory(); S.items = S.items.filter(i => i.id !== it.id);
  purgeRefs(); renderItemsView(); refreshCount();
}
$('#itemNew').addEventListener('click', newItem);
$('#itemSearch').addEventListener('input', e => { S.itemSearch = e.target.value; renderItemGrid(); });
$('#iName').addEventListener('input', onItemNameInput);
$('#iName').addEventListener('blur', () => { const it = itemById(S.selItem); if (it) { $('#iName').value = it.name; setNameErr(''); } });
$('#iName').addEventListener('keydown', e => { if (e.key === 'Enter') e.target.blur(); });
$('#iCat').addEventListener('change', e => {
  const it = itemById(S.selItem); if (!it) return;
  pushHistory(); it.categoryId = e.target.value ? +e.target.value : null; renderItemsView();
});
$('#iPick').addEventListener('click', () => { if (itemById(S.selItem)) openSlicer('item'); });
$('#iFromTile').addEventListener('change', e => {
  const it = itemById(S.selItem), a = assetById(+e.target.value); e.target.value = '';
  if (!it || !a) return;
  pushHistory(); it.filename = a.filename; it.source = [...a.source]; renderItemsView();
});
$('#iClear').addEventListener('click', () => {
  const it = itemById(S.selItem); if (!it || !it.source) return;
  pushHistory(); it.filename = null; it.source = null; renderItemsView();
});
$('#iDelete').addEventListener('click', deleteItem);

/* ---------- Spritesheet slicer (palette mode + item-pick mode) ---------- */
const SL = { mode: 'palette', sheet: null, zoom: 1, ox: 0, oy: 0, hover: null, current: null, dragStart: null, added: 0 };
const sc = $('#sliceCanvas'), sctx = sc.getContext('2d');
function slParams() {
  return {
    cw: +$('#slCW').value || 1, ch: +$('#slCH').value || 1, offx: +$('#slOX').value || 0, offy: +$('#slOY').value || 0,
    gx: +$('#slGX').value || 0, gy: +$('#slGY').value || 0, grid: $('#slGrid').checked,
  };
}
function openSlicer(mode) {
  SL.mode = mode;
  const item = mode === 'item' ? itemById(S.selItem) : null;
  $('#sliceTitle').textContent = item ? `Pick a sprite for ${item.name}` : 'Slice spritesheet';
  $('#slAddRow').classList.toggle('hidden', mode === 'item');
  $('#slPickRow').classList.toggle('hidden', mode !== 'item');
  $('#sliceDone').textContent = mode === 'item' ? 'Cancel' : 'Done';
  if (item && sheetByFile(item.filename)) SL.sheet = sheetByFile(item.filename);
  if (!S.sheets.includes(SL.sheet)) SL.sheet = S.sheets[S.sheets.length - 1] || null;
  refreshSheetSel();
  SL.current = item && item.source && SL.sheet && item.filename === SL.sheet.filename
    ? { x: item.source[0], y: item.source[1], w: item.source[2], h: item.source[3] } : null;
  SL.added = 0; $('#slCountLbl').textContent = '0 sprites added';
  updateSlInfo();
  $('#sliceModal').classList.add('open');
  requestAnimationFrame(() => { sizeSliceCanvas(); slRender(); });
}
function closeSlicer() { $('#sliceModal').classList.remove('open'); }
function refreshSheetSel() {
  const sel = $('#sheetSel');
  sel.innerHTML = S.sheets.length ? S.sheets.map(s => `<option value="${s.id}">${esc(s.name)} (${s.w}×${s.h})</option>`).join('') : '<option>No sheets yet. Upload one.</option>';
  if (SL.sheet) sel.value = SL.sheet.id;
}
$('#sliceBtn').addEventListener('click', () => openSlicer('palette'));
$('#sliceClose').addEventListener('click', closeSlicer);
$('#sliceDone').addEventListener('click', () => {
  closeSlicer();
  if (SL.mode === 'palette' && !S.selAsset && S.assets.length) selectAsset(S.assets[S.assets.length - 1].id);
});
$('#sliceModal').addEventListener('click', e => { if (e.target.id === 'sliceModal') closeSlicer(); });
$('#sheetSel').addEventListener('change', () => { SL.sheet = S.sheets.find(s => s.id === +$('#sheetSel').value) || null; SL.current = null; updateSlInfo(); sizeSliceCanvas(); slRender(); });
$('#sheetUpload').addEventListener('click', () => $('#sheetFileInput').click());
$('#sheetFileInput').addEventListener('change', async e => {
  const f = e.target.files[0]; e.target.value = '';
  if (!f) return;
  SL.sheet = await addSheet(await readFile(f), f.name);   // sheet only — no palette tile is created
  SL.current = null; refreshSheetSel(); updateSlInfo(); sizeSliceCanvas(); slRender(); afterLibraryChange();
});
['#slCW', '#slCH', '#slOX', '#slOY', '#slGX', '#slGY', '#slGrid'].forEach(id => $(id).addEventListener('input', () => { SL.current = null; updateSlInfo(); slRender(); }));

function sizeSliceCanvas() {
  const r = sc.getBoundingClientRect();
  sc.width = Math.round(r.width * DPR); sc.height = Math.round(r.height * DPR);
  if (SL.sheet) {
    const s = SL.sheet;
    SL.zoom = Math.min(4, (r.width - 20) / s.w, (r.height - 20) / s.h);
    SL.ox = (r.width - s.w * SL.zoom) / 2; SL.oy = (r.height - s.h * SL.zoom) / 2;
  }
}
const slToSheet = (cx, cy) => ({ x: (cx - SL.ox) / SL.zoom, y: (cy - SL.oy) / SL.zoom });
function cellAt(sx, sy) {
  const p = slParams(), stepx = p.cw + p.gx, stepy = p.ch + p.gy;
  if (!SL.sheet || sx < p.offx || sy < p.offy) return null;
  const c = Math.floor((sx - p.offx) / stepx), r = Math.floor((sy - p.offy) / stepy);
  if ((sx - p.offx) - c * stepx > p.cw || (sy - p.offy) - r * stepy > p.ch) return null;
  const x = p.offx + c * stepx, y = p.offy + r * stepy;
  if (x >= SL.sheet.w || y >= SL.sheet.h) return null;
  return { x, y, w: p.cw, h: p.ch };
}
function slRender() {
  sctx.setTransform(1, 0, 0, 1, 0, 0); sctx.clearRect(0, 0, sc.width, sc.height); sctx.setTransform(DPR, 0, 0, DPR, 0, 0);
  const rr = sc.getBoundingClientRect();
  if (!SL.sheet) { sctx.fillStyle = '#6a6f7a'; sctx.font = '13px system-ui'; sctx.textAlign = 'center'; sctx.fillText('Upload a spritesheet to begin.', rr.width / 2, rr.height / 2); return; }
  sctx.save(); sctx.translate(SL.ox, SL.oy); sctx.scale(SL.zoom, SL.zoom);
  sctx.imageSmoothingEnabled = false; sctx.drawImage(SL.sheet.img, 0, 0);
  const p = slParams();
  if (p.grid && p.cw > 0 && p.ch > 0) {
    const stepx = p.cw + p.gx, stepy = p.ch + p.gy;
    sctx.strokeStyle = 'rgba(90,169,255,0.35)'; sctx.lineWidth = 1 / SL.zoom; sctx.beginPath();
    for (let x = p.offx; x <= SL.sheet.w + 0.5; x += stepx) { sctx.moveTo(x, 0); sctx.lineTo(x, SL.sheet.h); if (p.gx) { sctx.moveTo(x + p.cw, 0); sctx.lineTo(x + p.cw, SL.sheet.h); } }
    for (let y = p.offy; y <= SL.sheet.h + 0.5; y += stepy) { sctx.moveTo(0, y); sctx.lineTo(SL.sheet.w, y); if (p.gy) { sctx.moveTo(0, y + p.ch); sctx.lineTo(SL.sheet.w, y + p.ch); } }
    sctx.stroke();
  }
  if (SL.hover) { sctx.fillStyle = 'rgba(62,207,142,0.18)'; sctx.fillRect(SL.hover.x, SL.hover.y, SL.hover.w, SL.hover.h); }
  if (SL.current) {
    const c = SL.current;
    sctx.fillStyle = 'rgba(62,207,142,0.15)'; sctx.fillRect(c.x, c.y, c.w, c.h);
    sctx.strokeStyle = '#3ecf8e'; sctx.lineWidth = 2 / SL.zoom; sctx.strokeRect(c.x, c.y, c.w, c.h);
  }
  sctx.restore();
}
sc.addEventListener('mousemove', e => {
  if (!SL.sheet) return;
  const r = sc.getBoundingClientRect(), sp = slToSheet(e.clientX - r.left, e.clientY - r.top);
  if (SL.dragStart) {
    const a = SL.dragStart, b = sp;
    if (slParams().grid) {
      const ca = cellAt(Math.min(a.x, b.x), Math.min(a.y, b.y)) || cellAt(a.x, a.y);
      const cb = cellAt(Math.max(a.x, b.x), Math.max(a.y, b.y)) || cellAt(b.x, b.y);
      if (ca && cb) { const x = Math.min(ca.x, cb.x), y = Math.min(ca.y, cb.y); SL.current = { x, y, w: Math.max(ca.x + ca.w, cb.x + cb.w) - x, h: Math.max(ca.y + ca.h, cb.y + cb.h) - y }; }
    } else {
      SL.current = { x: Math.round(Math.min(a.x, b.x)), y: Math.round(Math.min(a.y, b.y)), w: Math.round(Math.abs(b.x - a.x)), h: Math.round(Math.abs(b.y - a.y)) };
    }
    updateSlInfo();
  } else SL.hover = cellAt(sp.x, sp.y);
  slRender();
});
sc.addEventListener('mousedown', e => { if (!SL.sheet) return; const r = sc.getBoundingClientRect(); SL.dragStart = slToSheet(e.clientX - r.left, e.clientY - r.top); });
window.addEventListener('mouseup', e => {
  if (!SL.dragStart || !SL.sheet) { SL.dragStart = null; return; }
  const r = sc.getBoundingClientRect(), sp = slToSheet(e.clientX - r.left, e.clientY - r.top);
  if (Math.abs(sp.x - SL.dragStart.x) <= 3 && Math.abs(sp.y - SL.dragStart.y) <= 3) SL.current = cellAt(sp.x, sp.y);
  SL.dragStart = null; updateSlInfo(); slRender();
});
function updateSlInfo() {
  const c = SL.current;
  const txt = c ? `Region: [${Math.round(c.x)}, ${Math.round(c.y)}, ${Math.round(c.w)}, ${Math.round(c.h)}]` : 'Region: none (click a cell or drag)';
  $('#slSelInfo').textContent = txt; $('#slPickInfo').textContent = txt;
  if (c && SL.mode === 'palette' && !$('#slName').value) $('#slName').value = `${SL.sheet ? SL.sheet.name : 'sprite'}_${Math.round(c.x)}_${Math.round(c.y)}`;
}
$('#slAdd').addEventListener('click', () => {
  const c = SL.current;
  if (!SL.sheet || !c || c.w < 1 || c.h < 1) { toast('Pick a region first.'); return; }
  addSpriteAsset(SL.sheet, Math.round(c.x), Math.round(c.y), Math.round(c.w), Math.round(c.h), $('#slName').value.trim() || undefined);
  SL.added++; $('#slCountLbl').textContent = `${plural(SL.added, 'sprite', 'sprites')} added`; $('#slName').value = '';
  renderAssets();
});
$('#slAddAll').addEventListener('click', () => {
  if (!SL.sheet) return;
  const p = slParams(), stepx = p.cw + p.gx, stepy = p.ch + p.gy;
  let n = 0;
  for (let y = p.offy; y + p.ch <= SL.sheet.h + 0.5; y += stepy)
    for (let x = p.offx; x + p.cw <= SL.sheet.w + 0.5; x += stepx) {
      addSpriteAsset(SL.sheet, x, y, p.cw, p.ch, `${SL.sheet.name}_${Math.round((x - p.offx) / stepx)}_${Math.round((y - p.offy) / stepy)}`); n++;
    }
  SL.added += n; $('#slCountLbl').textContent = `${plural(SL.added, 'sprite', 'sprites')} added`;
  renderAssets(); toast(`Added ${plural(n, 'cell', 'cells')} to the palette.`);
});
function setItemSprite(r) {
  const it = itemById(S.selItem);
  if (!it || !SL.sheet) return;
  if (!r || r.w < 1 || r.h < 1) { toast('Pick a region first.'); return; }
  pushHistory();
  it.filename = SL.sheet.filename;
  it.source = [Math.round(r.x), Math.round(r.y), Math.round(r.w), Math.round(r.h)];
  closeSlicer(); renderItemsView();
}
$('#slUse').addEventListener('click', () => setItemSprite(SL.current));
$('#slUseWhole').addEventListener('click', () => SL.sheet && setItemSprite({ x: 0, y: 0, w: SL.sheet.w, h: SL.sheet.h }));

/* ---------- Export ---------- */
function buildJSON() {
  const P = S.pathPrefix || '';
  const layerIndex = new Map(S.layers.map((L, i) => [L.id, i]));
  const tiles = [];
  for (const L of S.layers) for (const t of S.tiles) {
    if (t.layerId !== L.id) continue;
    const a = assetById(t.assetId);
    const o = {
      id: t.id,
      image: P + (a ? a.filename : 'missing.png'),
      layer: layerIndex.get(L.id),
      source: a && a.source ? a.source.map(Math.round) : [0, 0, Math.round(t.w), Math.round(t.h)],
      size: [Math.round(t.w), Math.round(t.h)],
      location: [Math.round(t.x), Math.round(t.y)],
    };
    if (tileAttrs(t).length) o.attributes = [...t.attrs];
    const inv = tileInv(t).filter(e => itemById(e.itemId)).map(e => ({ item: itemById(e.itemId).name, count: e.count }));
    if (inv.length) o.inventory = inv;
    tiles.push(o);
  }
  return {
    width: String(S.mapW),
    height: String(S.mapH),
    layers: S.layers.map(L => ({ name: L.name, visible: L.visible })),
    categories: S.categories.map(c => c.name),
    items: S.items.map(it => {
      const c = catById(it.categoryId);
      return { name: it.name, category: c ? c.name : null, image: it.filename ? P + it.filename : null, source: it.source ? it.source.map(Math.round) : null };
    }),
    tiles,
    paths: {
      nodes: S.nodes.map(n => {
        const o = { id: n.id, location: [Math.round(n.x), Math.round(n.y)] };
        const tg = n.targets.filter(tileById);
        if (tg.length) o.targets = tg;
        return o;
      }),
      edges: S.edges.filter(([a, b]) => nodeById(a) && nodeById(b)).map(([a, b]) => [a, b]),
    },
  };
}

/* ---------- Import ---------- */
/** Keep ids from the file where possible; give missing or duplicate ids a fresh one. */
function reserveIds(raw, prefix) {
  const re = new RegExp(`^${prefix}(\\d+)$`);
  const wanted = raw.map(o => (o && typeof o.id === 'string' && o.id.trim() ? o.id.trim() : null));
  const all = new Set(wanted.filter(Boolean)), taken = new Set();
  let seq = 1, dupes = 0;
  for (const w of wanted) { const m = w && re.exec(w); if (m) seq = Math.max(seq, +m[1] + 1); }
  const ids = wanted.map(w => {
    if (w && !taken.has(w)) { taken.add(w); return w; }
    if (w) dupes++;
    let id; do { id = prefix + seq++; } while (all.has(id) || taken.has(id));
    taken.add(id); return id;
  });
  S.seq[prefix] = seq;
  return { ids, dupes };
}
function importJSON(text) {
  let data;
  try { data = JSON.parse(text); } catch (err) { alert('That is not valid JSON: ' + err.message); return; }
  const warn = [];
  const arr = v => (Array.isArray(v) ? v : []);

  S.mapW = parseInt(data.width, 10) || S.mapW; S.mapH = parseInt(data.height, 10) || S.mapH;
  $('#mapW').value = S.mapW; $('#mapH').value = S.mapH;

  const rawLayers = arr(data.layers).length ? data.layers : [{ name: 'Layer 1' }];
  S.layers = rawLayers.map((L, i) => ({ id: nid(), name: (L && L.name) || `Layer ${i + 1}`, visible: !L || L.visible !== false, locked: false }));
  S.activeLayer = S.layers[0].id;

  // Categories (items may also introduce ones missing from the list)
  S.categories = [];
  const ensureCat = name => {
    if (name == null || !String(name).trim()) return null;
    let c = catByName(String(name).trim());
    if (!c) { c = { id: nid(), name: String(name).trim() }; S.categories.push(c); }
    return c.id;
  };
  arr(data.categories).forEach(ensureCat);

  // Items — names must be unique
  S.items = [];
  for (const it of arr(data.items)) {
    const name = String((it && it.name) || '').trim();
    if (!name) { warn.push('skipped an item without a name'); continue; }
    if (itemByName(name)) { warn.push(`skipped duplicate item ${name}`); continue; }
    S.items.push({
      id: nid(), name, categoryId: ensureCat(it.category),
      filename: it.image ? basename(it.image) : null,
      source: Array.isArray(it.source) ? it.source.slice(0, 4).map(Number) : null,
    });
  }

  // Tiles
  const rawTiles = arr(data.tiles);
  const { ids: tIds, dupes: tDupes } = reserveIds(rawTiles, 't');
  if (tDupes) warn.push(`${plural(tDupes, 'duplicate tile id was', 'duplicate tile ids were')} renumbered`);
  let missingInv = 0;
  S.tiles = rawTiles.map((t, i) => {
    const size = Array.isArray(t.size) ? t.size : [S.grid, S.grid], loc = Array.isArray(t.location) ? t.location : [0, 0];
    const file = basename(t.image) || 'missing.png', sh = sheetByFile(file);
    const src = Array.isArray(t.source) ? t.source.slice(0, 4).map(Number) : sh ? [0, 0, sh.w, sh.h] : [0, 0, +size[0] || S.grid, +size[1] || S.grid];
    let a = S.assets.find(x => x.filename === file && sameSrc(x.source, src));
    if (!a) {
      const whole = sh && sameSrc(src, [0, 0, sh.w, sh.h]);
      const name = file.replace(/\.[^.]+$/, '') + (whole ? '' : `_${src[0]}_${src[1]}`);
      a = sh ? addSpriteAsset(sh, src[0], src[1], src[2], src[3], name)
             : addAssetObj({ name, filename: file, img: null, dataURL: '', w: src[2], h: src[3], source: src });
    }
    const li = typeof t.layer === 'number' ? clamp(Math.round(t.layer), 0, S.layers.length - 1) : 0;
    const inv = [];
    for (const e of arr(t.inventory)) {
      const it = e && itemByName(String(e.item));
      if (!it) { missingInv++; continue; }
      const cnt = Math.max(1, parseInt(e.count, 10) || 1), ex = inv.find(x => x.itemId === it.id);
      if (ex) ex.count += cnt; else inv.push({ itemId: it.id, count: cnt });
    }
    return {
      id: tIds[i], assetId: a.id, layerId: S.layers[li].id,
      x: +loc[0] || 0, y: +loc[1] || 0, w: +size[0] || S.grid, h: +size[1] || S.grid,
      attrs: [...new Set(arr(t.attributes).map(String))], inv,
    };
  });
  if (missingInv) warn.push(`${plural(missingInv, 'inventory entry refers', 'inventory entries refer')} to unknown items`);

  // Paths
  const P = data.paths || {};
  const rawNodes = arr(P.nodes);
  const { ids: nIds, dupes: nDupes } = reserveIds(rawNodes, 'n');
  if (nDupes) warn.push(`${plural(nDupes, 'duplicate node id was', 'duplicate node ids were')} renumbered`);
  let badTargets = 0;
  S.nodes = rawNodes.map((n, i) => {
    const loc = Array.isArray(n.location) ? n.location : [0, 0];
    const targets = [...new Set(arr(n.targets).map(String))].filter(id => (tileById(id) ? true : (badTargets++, false)));
    return { id: nIds[i], x: clamp(+loc[0] || 0, 0, S.mapW), y: clamp(+loc[1] || 0, 0, S.mapH), targets };
  });
  if (badTargets) warn.push(`${plural(badTargets, 'node target points', 'node targets point')} to unknown tiles`);
  S.edges = [];
  for (const e of arr(P.edges)) {
    const [a, b] = Array.isArray(e) ? e : [e && e.from, e && e.to];
    if (nodeById(a) && nodeById(b)) addEdge(a, b);
  }

  Object.assign(S, { sel: [], nsel: [], linkFrom: null, selItem: null, itemFilter: 'all', history: [], future: [] });
  closeModals(); renderAssets(); refreshAll(); fitView();
  toast(warn.length ? `Imported with notes: ${warn.join('; ')}.` : 'Map imported.');
}

/* ---------- Export / import modal ---------- */
function openModal(mode) {
  const foot = $('#modalFoot'); foot.innerHTML = '';
  const btn = (label, fn, cls) => { const b = document.createElement('button'); b.textContent = label; if (cls) b.className = cls; b.onclick = fn; foot.appendChild(b); return b; };
  if (mode === 'export') {
    $('#modalTitle').textContent = 'Export JSON';
    $('#jsonView').classList.remove('hidden'); $('#jsonInput').classList.add('hidden');
    const json = JSON.stringify(buildJSON(), null, 2);
    $('#jsonView').textContent = json;
    const copy = btn('Copy', () => { navigator.clipboard.writeText(json); copy.textContent = 'Copied'; setTimeout(() => (copy.textContent = 'Copy'), 1200); });
    btn('Download .json', () => {
      const url = URL.createObjectURL(new Blob([json], { type: 'application/json' }));
      const a = document.createElement('a'); a.href = url; a.download = 'supermarket-map.json'; a.click(); URL.revokeObjectURL(url);
    }, 'primary');
  } else {
    $('#modalTitle').textContent = 'Import JSON';
    $('#jsonView').classList.add('hidden'); $('#jsonInput').classList.remove('hidden'); $('#jsonInput').value = '';
    btn('Load map', () => importJSON($('#jsonInput').value), 'primary');
  }
  $('#modal').classList.add('open');
}
const anyModalOpen = () => !!document.querySelector('.modal.open');
function closeModals() { $$('.modal.open').forEach(m => m.classList.remove('open')); }
$('#exportBtn').addEventListener('click', () => openModal('export'));
$('#importBtn').addEventListener('click', () => openModal('import'));
$('#modalClose').addEventListener('click', closeModals);
$('#modal').addEventListener('click', e => { if (e.target.id === 'modal') closeModals(); });

/* ---------- Views, tools, toolbar ---------- */
const HINTS = {
  map: 'Drag empty space to box-select · Shift-click to add or remove · Space or middle mouse to pan',
  select: 'Drag nodes to move them · drag empty space to box-select · Shift-click to add or remove',
  node: 'Click to add a node · Shift-click to add one connected to the selected node',
  link: 'Click a node, then another node to connect it, or an object to target it',
};
function setView(v) {
  S.view = v; document.body.dataset.view = v;
  $$('#viewTabs button').forEach(b => b.classList.toggle('on', b.dataset.view === v));
  S.linkFrom = null; drag = null;
  if (v === 'items') renderItemsView(); else syncInspector();
  updateMode(); draw();
}
function setTool(t) {
  S.tool = t;
  $$('#toolSeg button').forEach(b => { const on = b.dataset.tool === t; b.classList.toggle('on', on); b.classList.toggle('paint', on && t === 'paint'); });
  updateMode(); updateCursorReset();
}
function setPTool(t) {
  S.ptool = t; if (t !== 'link') S.linkFrom = null;
  $$('#ptoolSeg button').forEach(b => b.classList.toggle('on', b.dataset.ptool === t));
  updateMode(); draw();
}
function updateMode() {
  const v = S.view;
  $('#stMode').textContent = v === 'items' ? 'Items' : v === 'paths' ? `Paths · ${cap(S.ptool)} tool` : `${cap(S.tool)} mode`;
  $('#hint').textContent = v === 'paths' ? HINTS[S.ptool] : HINTS.map;
}
$('#viewTabs').addEventListener('click', e => { const b = e.target.closest('button'); if (b) setView(b.dataset.view); });
$('#toolSeg').addEventListener('click', e => { const b = e.target.closest('button'); if (b) setTool(b.dataset.tool); });
$('#ptoolSeg').addEventListener('click', e => { const b = e.target.closest('button'); if (b) setPTool(b.dataset.ptool); });
$('#snapChk').addEventListener('change', e => { S.snap = e.target.checked; });
$('#gridChk').addEventListener('change', e => { S.showGrid = e.target.checked; draw(); });
$('#gridSize').addEventListener('input', e => { const v = parseInt(e.target.value, 10); if (v > 0) { S.grid = v; draw(); } });
$('#mapW').addEventListener('input', e => { const v = parseInt(e.target.value, 10); if (v > 0) { beginMutation(); S.mapW = v; draw(); } });
$('#mapH').addEventListener('input', e => { const v = parseInt(e.target.value, 10); if (v > 0) { beginMutation(); S.mapH = v; draw(); } });
$('#pathPrefix').addEventListener('input', e => { S.pathPrefix = e.target.value; });
$('#undoBtn').addEventListener('click', undo);
$('#redoBtn').addEventListener('click', redo);

/* ---------- Zoom ---------- */
function updateZoomLabel() { const p = Math.round(S.zoom * 100) + '%'; $('#zoomVal').textContent = p; $('#stZoom').textContent = p; }
function setZoom(z, cx, cy) {
  z = clamp(z, 0.1, 8);
  const r = canvas.getBoundingClientRect();
  cx = cx ?? r.width / 2; cy = cy ?? r.height / 2;
  const before = toMap(cx, cy); S.zoom = z; const after = toMap(cx, cy);
  S.panX += (after.x - before.x) * S.zoom; S.panY += (after.y - before.y) * S.zoom;
  updateZoomLabel(); draw();
}
function fitView() {
  const r = canvas.getBoundingClientRect();
  if (!r.width || !r.height) { S.needFit = true; return; }   // canvas hidden (Items view) — fit when it shows again
  S.needFit = false;
  const pad = 40;
  S.zoom = clamp(Math.min((r.width - pad * 2) / S.mapW, (r.height - pad * 2) / S.mapH), 0.1, 8);
  S.panX = (r.width - S.mapW * S.zoom) / 2; S.panY = (r.height - S.mapH * S.zoom) / 2;
  updateZoomLabel(); draw();
}
canvas.addEventListener('wheel', e => { e.preventDefault(); const r = canvas.getBoundingClientRect(); setZoom(S.zoom * (e.deltaY < 0 ? 1.12 : 1 / 1.12), e.clientX - r.left, e.clientY - r.top); }, { passive: false });
$('#zoomIn').addEventListener('click', () => setZoom(S.zoom * 1.2));
$('#zoomOut').addEventListener('click', () => setZoom(S.zoom / 1.2));
$('#fitBtn').addEventListener('click', fitView);

/* ---------- Keyboard ---------- */
const isTextEntry = el => !!el && (el.tagName === 'TEXTAREA' || (el.tagName === 'INPUT' && !['checkbox', 'radio', 'button', 'file'].includes(el.type)));
const inField = () => { const a = document.activeElement; return isTextEntry(a) || (!!a && a.tagName === 'SELECT'); };

window.addEventListener('keydown', e => {
  const mod = e.ctrlKey || e.metaKey, k = e.key.toLowerCase();
  if (e.key === 'Escape' && anyModalOpen()) { closeModals(); return; }
  if (anyModalOpen()) return;
  if (mod && (k === 'z' || k === 'y') && !isTextEntry(document.activeElement)) {
    if (k === 'y' || e.shiftKey) redo(); else undo();
    e.preventDefault(); return;
  }
  if (inField()) return;
  if (e.code === 'Space' && S.view !== 'items') { spaceDown = true; if (!drag) canvas.style.cursor = 'grab'; e.preventDefault(); return; }
  if (S.view === 'map') mapKeys(e, mod, k);
  else if (S.view === 'paths') pathKeys(e, mod, k);
});
window.addEventListener('keyup', e => {
  if (e.code === 'Space') { spaceDown = false; updateCursorReset(); }
  if (e.key.startsWith('Arrow')) S.editing = false;   // one undo step per held arrow key
});
function nudge(e, objs) {
  const s = e.shiftKey ? S.grid : 1;
  const dx = e.key === 'ArrowLeft' ? -s : e.key === 'ArrowRight' ? s : 0, dy = e.key === 'ArrowUp' ? -s : e.key === 'ArrowDown' ? s : 0;
  const c = clampDelta(objs, dx, dy);
  if (c.dx || c.dy) { beginMutation(); for (const o of objs) { o.x += c.dx; o.y += c.dy; } }
  syncTransformFields(); draw(); e.preventDefault();
}
function mapKeys(e, mod, k) {
  if (mod && k === 'a') { setSel(renderList().filter(t => selectableLayer(t.layerId)).map(t => t.id)); e.preventDefault(); return; }
  if (mod && k === 'd') { if (S.sel.length) { dupSel(); e.preventDefault(); } return; }
  if (mod) return;
  if (e.key === 'Delete' || e.key === 'Backspace') { if (S.sel.length) { delSel(); e.preventDefault(); } return; }
  if (k === 'v') setTool('select');
  else if (k === 'b') setTool('place');
  else if (k === 'p') setTool('paint');
  else if (k === 'f') fitView();
  else if (e.key === 'Escape') clearSel();
  else if (e.key.startsWith('Arrow') && S.sel.length) nudge(e, selTiles());
}
function pathKeys(e, mod, k) {
  if (mod && k === 'a') { setNSel(S.nodes.map(n => n.id)); e.preventDefault(); return; }
  if (mod) return;
  if (e.key === 'Delete' || e.key === 'Backspace') { if (S.nsel.length) { deleteNodes(); e.preventDefault(); } return; }
  if (k === 'v') setPTool('select');
  else if (k === 'n') setPTool('node');
  else if (k === 'l') setPTool('link');
  else if (k === 'f') fitView();
  else if (e.key === 'Escape') { S.linkFrom = null; setNSel([]); }
  else if (e.key.startsWith('Arrow') && S.nsel.length) nudge(e, selNodes());
}

/* ---------- Misc ---------- */
function refreshCount() {
  const targets = S.nodes.reduce((s, n) => s + n.targets.length, 0);
  $('#stCount').textContent = S.tiles.length; $('#stItems').textContent = S.items.length; $('#stNodes').textContent = S.nodes.length;
  $('#psNodes').textContent = S.nodes.length; $('#psEdges').textContent = S.edges.length; $('#psTargets').textContent = targets;
}
function refreshAll() {
  renderLayers(); syncInspector(); refreshCount(); updateUndoUI();
  if (S.view === 'items') renderItemsView();
  draw();
}
let toastTimer = null;
function toast(msg) {
  const el = $('#toast'); el.textContent = msg; el.classList.add('show');
  clearTimeout(toastTimer); toastTimer = setTimeout(() => el.classList.remove('show'), 2600);
}

/* ---------- Init ---------- */
S.layers = [
  { id: nid(), name: 'Ground', visible: true, locked: false },
  { id: nid(), name: 'Shelves', visible: true, locked: false },
];
S.activeLayer = S.layers[0].id;
setView('map'); setTool('select'); setPTool('select');
renderAssets(); refreshAll();
resizeCanvas(); fitView();
