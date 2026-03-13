# Vinayaka

A desktop stock ticker tracking application built with Spring Boot and JavaFX. Vinayaka enables users to monitor stock prices from NYSE, NASDAQ, and IEX exchanges using the Alpaca Markets API.

## Important Notice

- All market data displayed is subject to a 15-minute delay when using a free Alpaca Markets account.
- Data provided by Vinayaka is intended for informational purposes only and is NOT suitable for trading decisions. The developers assume NO responsibility for any financial losses or transactions made based on information displayed in this application.

## Features

### Ticker Management

- Add stock tickers from NYSE, NASDAQ, and IEX exchanges
- Remove tickers with confirmation dialog
- Mark tickers as favorites for prioritized display
- Automatic sorting with favorites first, followed by alphabetical order

### Price Display

- Real-time price updates with configurable refresh intervals (5, 10, 15, 30, or 60 seconds)
- Daily price change percentage with color-coded indicators (green for gains, red for losses)
- Intelligent market hours detection for accurate price calculations

### User Interface

- Dark and light mode toggle
- Timezone selection from all available system timezones
- Responsive grid layout for ticker display

### Data Management

- SQLite database with automatic initialization (no manual setup required)
- Persistent storage of user preferences and ticker lists
- Secure storage of Alpaca API credentials

## Roadmap

The following features are planned for future releases:

- Dedicated ticker detail window with interactive price charts (1D, 1W, 1M, 3M, 1Y, 5Y timeframes)
- Automatic detection of paid Alpaca Markets accounts for real-time data access
- Options chain display for supported tickers

## Prerequisites

- **Java 21** or later (Amazon Corretto, Eclipse Temurin, or Oracle JDK recommended)
- **Alpaca Markets Account** with API credentials
  - Sign up at [https://alpaca.markets](https://alpaca.markets)
  - Generate API keys from the dashboard

**Note:** Use API keys from your LIVE Alpaca account. Paper Trading API keys are not supported. It may take Alpaca 2-5 days for verification.

## Installation

### Option 1: Run with Maven Wrapper (Recommended)

Clone the repository and run using the included Maven wrapper:

```bash
git clone [https://github.com/ciyer17/Vinayaka.git](https://github.com/ciyer17/Vinayaka.git)
cd vinayaka
./mvnw spring-boot:run
```

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

### Option 2: Build and Run JAR

```bash
./mvnw clean package
java -jar target/vinayaka-0.0.1-SNAPSHOT.jar
```

### First Launch

On first launch, Vinayaka will prompt for your Alpaca API credentials. Enter your API Key (26 characters) and API Secret (44 characters) from your Alpaca Markets dashboard.
API credentials can be updated at any time through the Settings page. Credentials are validated against Alpaca's servers before being saved.

The application automatically creates its database at:

- **Linux/macOS:** `~/.config/vinayaka/vinayaka.db`
- **Windows:** `%APPDATA%\Vinayaka\vinayaka.db`

## Configuration

Access settings through the gear icon in the main window:

| Setting          | Description                              | Default          |
|------------------|------------------------------------------|------------------|
| Refresh Interval | Frequency of price updates (in seconds)  | 10               |
| Dark Mode        | Toggle between dark and light themes     | Enabled          |
| Timezone         | Timezone for time displays               | America/New_York |

## Usage

### Adding Tickers

1. Enter a stock symbol in the search field at the top of the main window
2. Click the search icon or press Enter
3. The ticker will appear in the grid if it exists on supported exchanges

### Managing Tickers

- **Favorite:** Hover over a ticker and click the heart icon to toggle favorite status
- **Delete:** Hover over a ticker and click the trash icon, then confirm deletion in the dialog

### Viewing Prices

Tickers display:

- Current price (closing price or 15-minute delayed intraday price)
- Daily percentage change from previous trading day close
- Color coding: green indicates positive change, red indicates negative change

## Building from Source

### Requirements

- Java 21 JDK
- Maven 3.9+ (or use the included Maven wrapper)

### Build Commands

```bash
# Compile and run tests
./mvnw clean verify

# Package as JAR
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run with JavaFX Maven plugin
./mvnw javafx:run
```

### IDE Setup

Import the project as a Maven project. The following IDE configurations are recommended:

- **IntelliJ IDEA:** Import as Maven project; enable annotation processing for Lombok
- **Eclipse:** Install Lombok plugin; import as existing Maven project
- **VS Code:** Install Extension Pack for Java and Lombok Annotations Support

## Project Structure

```
src/main/java/com/iyer/vinayaka/
├── config/          # Application configuration (database setup)
├── controller/      # JavaFX controllers for UI views
├── entities/        # JPA entities (UserSettings, UserTickers)
├── events/          # Spring application events
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic and API integration
└── util/            # Utility classes and helpers

src/main/resources/
├── view/            # FXML layout files
├── css/             # Application stylesheets
├── icons/           # UI icons
└── application.properties
```

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Icons provided by [Icons8](https://icons8.com)
- Market data provided by [Alpaca Markets](https://alpaca.markets)
- Built with [Spring Boot](https://spring.io/projects/spring-boot) and [JavaFX](https://openjfx.io)
