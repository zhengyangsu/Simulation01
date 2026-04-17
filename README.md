# Robot Simulation - Phase 4

A Java-based graphical simulation featuring autonomous robots, hunters, and energy management. This project demonstrates complex agent behaviors, state machines, and interactive simulation environments using Java Swing and Graphics2D.

## Features

- **Autonomous Robots**: Robots navigate the environment to collect dust piles for energy while avoiding predators.
- **Predator-Prey Dynamics**: 
    - `HunterBot`: Specialized machines that hunt and eliminate robots.
    - `Hunter`: A powerful entity that can be controlled or automated to fire missiles.
- **State-Based Behavior**: Entities utilize state machines to transition between states such as `SEARCHING`, `HUNTING`, `ESCAPING`, and `AVOIDING`.
- **Energy System**: Robots consume energy during movement and must replenish it by collecting `DustPiles`. They exhibit different behaviors based on energy levels (e.g., flickering when weak).
- **Interactive Environment**:
    - **Debug Mode**: Toggle visible debug information (hitboxes, states, FOV) by clicking the "Debug" button.
    - **Add Dust**: Double-click anywhere in the room to spawn a new dust pile.
    - **Enlarge Dust**: `Ctrl + Click` on a dust pile to increase its size.
    - **Combat**: Press `Space` to fire missiles from the Hunter (when active).
- **Physics & Steering**: Implements wall repulsion, obstacle avoidance, and target acquisition using `PVector` for smooth movement.

## Project Structure

- `RobotApp.java`: The main entry point that initializes the JFrame.
- `RobotPane.java`: The core simulation engine and rendering panel.
- `Machine.java`: Abstract base class for all simulated entities.
- `Robot.java`: Implementation of the prey agent (dust collector).
- `Hunter.java` & `HunterBot.java`: Implementation of predator agents.
- `DustPile.java`: Energy sources for robots.
- `Room.java`: Handles environment boundaries and physics.
- `Missile.java`: Projectiles fired by the Hunter.

## Requirements

- Java JDK 8 or higher.
- `processing-core` library (included in `lib/`).

## How to Run

1.  **Compile**: Ensure the `lib/core.jar` (or equivalent Processing core library) is in your classpath.
2.  **Execute**: Run the `RobotApp` class.
    ```bash
    java -cp "bin;lib/*" RobotApp
    ```

## Controls

- **Space**: Fire missiles (Hunter).
- **Mouse Double-Click**: Spawn DustPile.
- **Ctrl + Mouse Click**: Enlarge DustPile.
- **UI Button**: Toggle Debug Info.

---
*Developed as part of Simulation01 - Phase 4.*
