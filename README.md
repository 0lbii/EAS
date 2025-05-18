
## Enunciado

Para clonar esta versión directamente en su repositorio privado, existen varias alternativas, pero la más sencilla es hacer simplemente lo siguiente (asumiendo que el repositorio destino se llama [https://github.com/Sw-Evolution/25EXX](https://github.com/Sw-Evolution/)):

    $ git clone git://github.com/Sw-Evolution/Stratego.git
    $ cd Stratego
    $ git remote remove origin
    $ git remote add origin https://github.com/Sw-Evolution/25EXX.git
    $ git push -u origin master

De modo que se inicia el repositorio privado (no se puede hacer directamente un *fork* del repositorio público) con la versión completa del repositorio original, y poniendo esta misma versión como *commit* inicial, sin "arrastrar" la historia antigua del repositorio original (que sigue estando disponible en éste, por si alguien quisiera consultarla).

Por supuesto, en la versión final **no** es obligatorio que exista un directorio ``bin`` con la versión compilada del código; más bien, por el contrario, sería preferible que quedase claro cómo se compila el código. (En la versión original se explica aquí mismo, pero es un método que sólo funciona en una shell bash).

---

# <img width="400" alt="stratego" src="https://user-images.githubusercontent.com/26120940/32502146-37fad856-c397-11e7-80e1-a2edf3336774.png" />

> **Stratego** [/strəˈtiːɡoʊ/](https://en.wikipedia.org/wiki/Help:IPA/English) is a strategy board game for two players on a board of 10×10 squares. Each player controls 40 pieces representing individual officer ranks in an army. The objective of the game is to find and capture the opponent's *Flag*, or to capture so many enemy pieces that the opponent cannot make any further moves. *Stratego* has simple enough rules for young children to play, but a depth of strategy that is also appealing to adults.

[@nuttywhal](https://github.com/nuttywhal) and [@david-henderson](https://github.com/david-henderson) implemented this board game as a final project for SER 215 (Software Enterprise II) in Fall 2014 during our third semester at Arizona State University. It was written as a distributed application using a client–server model. The server awaits socket connections from two different clients and then dispatches a thread to handle a game session between those clients. The server is responsible for information security and enforcing the game rules so that players may not modify the game client in order to cheat.

## Building

```bash
# Clone the GitHub respository and `cd' into it.
git clone https://github.com/nuttywhal/stratego.git && cd stratego/

# Compile all of the *.java files into *.class files.
javac -cp src/edu/asu/stratego/**/*.java src/edu/asu/stratego/*.java -d temp/

# Copy all of the image assets.
cd src && cp --parents edu/asu/stratego/**/*.png ../temp && cd ..

# Create executable JAR files for the client and the server.
jar cvfm bin/client.jar src/manifest/client.mf -C temp/ .
jar cvfm bin/server.jar src/manifest/server.mf -C temp/ .

# Clean up.
rm -r temp/
```

… at this point in time, we did not know about build automation tools. :sob:

## Running

```bash
# Executing the client...
java -jar bin/client.jar &

# Executing the server...
java -jar bin/server.jar
```
##  🛠️ EXECUTION INSTRUCTIONS

1.  **Download the repository:**
    
    -   Clone or download this repository to your local machine
    ```
    $ git clone git://github.com/Sw-Evolution/Stratego.git
    $ cd Stratego
    $ git remote remove origin
    $ git remote add origin https://github.com/Sw-Evolution/25EXX.git
    $ git push -u origin master
    ```
      
2.  **Install Java 21:**
    
    -   If you don't have Java 21 installed, you can download it from the following link:  
        [Download Java 21](https://www.oracle.com/es/java/technologies/downloads/#java21)
        
3.  **Set up the database:**
    
    -   We are using XAMPP for the database, but you can use any SQL editor you prefer
    -   Create a database named `shop` and set the password `admin` for the `root` user
    
    In the **SQL Shell**, run the following commands:
    
    ```
    CREATE DATABASE shop;
    ALTER USER 'root'@'localhost' IDENTIFIED BY 'admin';
    ```
    
4.  **Run the project:**

## 📺 SCREENS
| Screen name    | Screen image      | Screen description      |
|:------------: |:------------:| :------------:|
| ![Image](https://github.com/user-attachments/assets/61731811-330f-40fa-b2f7-836c1ccbb7fe) | Login | This screen is used by users to login using their credentials |
| ![Image](https://github.com/user-attachments/assets/140962c2-d3ec-4e57-9fbe-13be0f4cc2b9) | Register | This screen allows users to create a new account |
| ![Image](https://github.com/user-attachments/assets/b0cfbe2e-6459-475d-bad6-265b72132861) | Menu | Main navigation screen to access all game options |
| ![Image](https://github.com/user-attachments/assets/6bf8d387-598d-46dd-b6b5-175e4ed5dbc1) | Set Up Game | Screen to configure and start a new game |
| ![Image](https://github.com/user-attachments/assets/ecc3d227-98b4-4da5-95bc-2b79a7815dec) | Game | Main gameplay screen where the match takes place |
| ![Image](https://github.com/user-attachments/assets/15c92215-eaa6-4e3c-9018-82eb1f90eb45) | Settings | Screen to change game settings and preferences |
| ![Image](https://github.com/user-attachments/assets/f71e4476-1914-4cc6-ba62-ec2082e9cc38) | Ranking | Shows the leaderboard with player scores and positions |
| ![Image](https://github.com/user-attachments/assets/259f23b9-d458-4f7c-85dd-19e8fea72839) | Game History | Displays a list of previously played games |
| ![Image](https://github.com/user-attachments/assets/90e15061-6c36-435c-bfd8-1330234c36ce) | Profile | Shows user profile information and statistics |
| ![Image](https://github.com/user-attachments/assets/4ec62961-0b90-409a-aed2-7a85fa12ee33) | Log out | Allows users to securely log out of the app |
