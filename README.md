# SmartGrid Multi-Agent System

A multi-agent system simulation of a smart electrical grid using the JADE (Java Agent DEvelopment Framework) platform. This project models the interactions between various components of a smart grid including power plants, smart buildings, load managers, and grids.

## Overview

SmartGrid simulates a realistic smart electrical grid environment where autonomous agents represent different entities in the power distribution network. The system models energy production, consumption, distribution, and management through agent communication and coordination.

### Key Features

- **Multi-Agent Architecture**: Built on JADE framework for distributed agent communication
- **Renewable & Non-Renewable Energy Sources**: Solar, wind, hydro, and diesel power plants
- **Smart Buildings**: Intelligent energy consumers with priority-based appliance management
- **Dynamic Weather Modeling**: Weather-dependent energy production for renewable sources
- **Load Management**: Intelligent distribution and routing of electrical energy
- **Real-time Monitoring**: Energy flow tracking and logging
- **Configurable Scenarios**: Build your own scenario or use existing ones (e.g. Tioman)

## Architecture

### Agent Types

#### Power Generation Agents
- **`PhotovoltaicPowerPlantAgent`**: Photovoltaic energy production based on weather conditions
- **`WindPowerPlantAgent`**: Wind energy generation with wind speed dependencies
- **`HydroPowerPlantAgent`**: Hydroelectric power generation
- **`DieselPowerPlantAgent`**: Traditional fossil fuel power

#### Grid Management Agents
- **`GridAgent`**: Manages energy distribution within buildings and other grids
- **`LoadManagerAgent`**: Coordinates energy allocation and load balancing

#### Consumer Agents
- **`SmartBuildingAgent`**: Represents intelligent buildings with controllable appliances
- **`SimulationAgent`**: Orchestrates the overall simulation and time management with a turn-based system

## Getting Started

### Prerequisites

- **Java 11** or higher
- **Maven 3.8.1** or higher
- **JADE Framework 4.6.0** (included as dependency)

### Installation and usage

1. **Clone the repository**
   ```bash
   git clone https://github.com/AlessandroIsceri/SmartGrid.git
   cd SmartGrid
   ```

2. **Set up the environment**
   ```bash
   # Use the provided batch script (Windows)
   run_config.bat
   ```
3. **Run the simulator**
   ```bash
   # On another terminal, run the following command
   mvn -Pjade-setup-simulation exec:java
   ```
4. **Start the simulation**

    Using the GUI, create a DummyAgent and send a message with a REQUEST performative and a conversation-id equal to "start-simulation" to the SimulationAgent.

5. **Stopping and Resuming the simulation**
    For stopping the simulation, the process is identical, you only have to change the conversation-id to "stop-simulation" and, naturally, the same applies to the resume message, which requires a "resume-simulation" conversation-id.

### Configuration

The simulation can be configured through `src/main/resources/app.config`:

```properties
turn_duration=00:30              # Duration of each simulation turn
interval_between_turns=500       # Milliseconds between turns
simulation_start_date=2025-04-01 # Simulation start date
latitude=2.792561903384909       # Geographic latitude (in degrees)
longitude=104.16942605504602     # Geographic longitude (in degrees)
scenario_name=Tioman             # Scenario to load
weather_turn_duration=01:00      # Weather update frequency
price_volatility=0.05            # Energy price volatility
price_trend=0.002                # Energy price trend
random_seed=42                   # Reproducible randomization
```

## Scenarios

Scenario files are located in `src/main/resources/scenarios/{scenario_name}/` and must contain the following files:
- `smartBuildings.json` - Building configurations and appliances
- `photovoltaicPowerPlants.json` - Photovoltaic panel specifications
- `windPowerPlants.json` - Wind turbine configurations
- `hydroPowerPlants.json` - Hydroelectric plant data
- `dieselPowerPlants.json` - Diesel generator specifications
- `grids.json` - Grid specifications
- `owners.json` - Owner specifications
- `cables.json` - Power transmission cables and layout information
- `loadManagers.json` - Load management specifications

An example is available at `src/main/resources/scenarios/Tioman/`

## Project Structure

```
SmartGrid/
├── src/main/java/com/ii/smartgrid/
│   ├── agents/                     # Agent implementations
│   ├── behaviours/                 # Agent behavior definitions
│   │   ├── grid/                   # Grid management behaviors
│   │   ├── loadmanager/            # Load management behaviors
│   │   ├── powerplant/             # Power generation behaviors
│   │   ├── simulation/             # Simulation control behaviors
│   │   └── smartbuilding/          # Smart building behaviors
│   ├── model/                      # Data models and entities
│   └── utils/                      # Utility classes
├── src/main/resources/
│   ├── scenarios/                  # Simulation scenarios
│   ├── output/                     # Simulation results [auto generated]
│   └── *.properties                # Configuration files
└── target/                         # Compiled classes and output
```

## Monitoring and Output

The system generates detailed logs and energy data:

- **Energy Data**: JSON files tracking energy flow over time (`output/{scenario_name}/energy_data-*.json`)
- **Simulation Logs**: Detailed agent communication logs (`simulationLog.log`)

## Dependencies

- **JADE Framework 4.6.0**: Multi-agent platform
- **Jackson 2.19.0**: JSON processing
- **SLF4J 2.0.17**: Logging framework
- **JGraphT 1.5.2**: Graph algorithms for grid topology
- **TimeZoneMap 4.5**: Geographic timezone handling


## Additional information

A report (in italian) is available [here](Progetto_Sistemi_Complessi__Modelli_e_Simulazione.pdf).