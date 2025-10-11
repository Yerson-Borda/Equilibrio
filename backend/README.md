How to run:

1- Input following command to terminal:

    ```
    python -m venv venv
    ```    

2- Make sure that you have selected .venv interpreter

3- Input following commands to terminal:

    ```
    Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process
    ```

    ```
    & "Your path here"/Equilibrio/.venv/Scripts/Activate.ps1
    ```

4- install all requirements in the requirements.txt file using following command:

    ```
    pip install -r requirements.txt
    ```

    Note: If you receive any errors, just pip install each component individually. Note that this project is made on the latest version of python.

5- make sure all requirements are downloaded

6- make a .env file in /backend and paste this information there:
    
    ```
    DATABASE_URL=sqlite:///./finance_tracker.db
    SECRET_KEY=your-secret-key-here
    ALGORITHM=HS256
    ACCESS_TOKEN_EXPIRE_MINUTES=30
    ```

    Note: You can enter anything in the secret key but to make a secure one, you can use the following code in terminal, copy and paste the string into .env

    ```
    python -c "import secrets; print(secrets.token_urlsafe(32))"
    ```

Run this in terminal

    ```uvicorn app.main:app --reload```

