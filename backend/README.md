How to run:

Input following commands to terminal:

    ```python -m venv venv```

    ```Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process```    

Make sure that you have selected .venv interpreter

Input following commands to terminal:

    ```Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process```

    ```& "Your path here"/Equilibrio/.venv/Scripts/Activate.ps1```

install all requirements in the requirements.txt file using pip

make sure all requirements are downloaded

Run this in terminal

    ```uvicorn app.main:app --reload```

