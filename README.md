# Gestor de Juegos - Parte del integrante 1

## Requisitos
- JDK 17 o superior.
- No requiere bibliotecas externas.

## Ejecutar desde la terminal
Desde la carpeta `GestorDeJuegos_Integrante1`:

```bash
javac -d out $(find src -name "*.java")
java -cp out main.Main
```

En Windows PowerShell se puede compilar con:

```powershell
$files = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
java -cp out main.Main
```

## Funcionalidad incluida
- Ventana principal Swing con `JDesktopPane`.
- Menú de selección de juegos mediante `JInternalFrame`.
- Interfaz compartida `core.GameFunction`.
- Clase compartida `core.Stat`.
- `core.GameManager` para registrar y abrir módulos.
- Juego Snake funcional con movimiento por flechas, comida, colisiones, puntaje y nombre del jugador.
- Singleton en `SnakeGame`.
- Pantalla temporal de récords que debe sustituirse al integrar el trabajo del integrante 2.

## Integración de los demás juegos
Los integrantes 2 y 3 deben implementar sus juegos como clases que extiendan `JInternalFrame` e implementen `GameFunction`. Cada juego debe exponer un método estático `getInstance()` que devuelva su Singleton.

Luego se registra desde `main.Main` antes de crear `MainFrame`, por ejemplo:

```java
gameManager.registrarJuego("Buscaminas", MinesweeperGame::getInstance);
gameManager.registrarJuego("Tres en raya", TicTacToeGame::getInstance);
```

Para el juego externo se registra el adaptador correspondiente de la misma forma.

## Importante para la integración
- No crear otra interfaz `GameFunction` ni otra clase `Stat`. Todos deben importar las clases del paquete `core`.
- El método `iniciar()` inicia o reinicia una partida.
- `getStats()` devuelve el resultado de la última partida terminada.
- El cierre de la ventana de cada juego debe ocultarla, no destruir la instancia. El Singleton conserva la misma instancia.
- La pantalla `RecordsPlaceholderFrame` es temporal. El integrante 2 debe reemplazarla o conectar su `RecordsFrame` desde `MainFrame`.
