# Vinayaka
A simple Java application for querying and managing stock tickers from NYSE, NASDAQ, and IEX exchanges using the Alpaca Markets API.

## IMPORTANT
- Currently, all listed data is 15-minute delayed due to assumption of usage of free Alpaca Markets accounts.
- Data listed on Vinayaka is **NOT** intended to be used for any trading activity. I am **NOT** responsible for the result of *any* transaction that happens as a result of the data shown on Vinayaka. Data listed on Vinayaka is only intended for users who wish to casually check on the status of their favorite stocks, in a centralized location.

## Setup
Currently, the project is in active development. So a lot of stuff needs to be done manually. In the future, it will be much simpler than this.<br/>

1. [Download] and install the Amazon Corretto JDK 21. Make sure to install the JDK and *not* the JRE.
2. Install an IDE (preferably IntelliJ IDEA) of your choice.
3. Install [MySQL] or [MySQL Workbench], login as the root user using the credentials you created during installation (in either the terminal or MySQL Workbench), and run the script setup.sql. You may change the default username and password to something of your choice if you wish to do so. <br/>
You may refer to [this] link for MySQL Workbench method, and [this one] for the terminal method. Keep in mind that for the terminal method, you need to focus only on running the script.
4. Clone and import the project into the IDE.
5. Resolve all Maven dependencies.
6. Now, in the application.properties file (inside the src/main/java/resources folder), replace the same existing lines with: <br>
    spring.datasource.username=vinayaka<br/>
    spring.datasource.password=vinayaka_108<br/><br/>
If you would like to set the same using Run Configurations, enter the following in the environment variables section. You don't have to make any modifications to the application.properties file.:<br/>
    MYSQL_USER=vinayaka<br/>
    MYSQL_PASSWORD=vinayaka\_108<br/><br/>
_If you used a different username and/or password, please input that username and/or password_

7. You should be able to run the application now. Make sure that the MySQL Server that you installed is running in the background, otherwise the application will crash.

## Planned Features
- Dedicated ticker window for each ticker showing a chart for 1D, 1W, 1M, 3M, 1Y, and 5Y historical bars.
- Ability to sort by price percentage change, alphabetical order, and favorited status.
- Ability to favorite tickers.
- Delete tickers.
- Implement a way to identify paid Alpaca Markets account users to get the latest data.
- Options chain for tickers (not fully sure about this yet).

## Attribution
Icons by [Icons8]. Thanks to them for offering great icons!

[Download]: https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html
[MySQL]: https://dev.mysql.com/downloads/mysql/
[MySQL Workbench]: https://dev.mysql.com/downloads/workbench/
[this]: https://www.tutorialspoint.com/how-to-run-sql-script-in-mysql
[this one]: https://sebhastian.com/mysql-running-sql-file/
[Icons8]: https://icons8.com/
