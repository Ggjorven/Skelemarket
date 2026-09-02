# Build & Run instructions

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/ggjorven/Skelemarket.git
    cd Skelemarket
    ```

2. Navigate to the scripts folder:
    ```sh
    cd scripts/windows
    ```

3. (Optional) If you haven't already installed the premake5 build system you can install it like this:
    ```sh
    ./install-premake5.bat
    ```

4. Generate Visual Studio project files:
    ```sh
    ./gen-vs2022.bat
    ```

## Building

1. Navigate to the root of the directory
2. Open the Puzzled-Skeleton.sln file
3. Start building in your desired configuration
4. Build files can be in the bin/%Config%-windows/Puzzled/ folder.
5. (Optional) Open a terminal and run the Sandbox project:
    ```sh
    ./Puzzled.exe 
    ```

## Running
