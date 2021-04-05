# Dartzee
All rights reserved

## Run

Yo, you don't have issues enabled, so thought I'd raise a PR instead. How do you run your app?

I tried importing into intellij and running the `application > run` task, but I get the following error:

```
> Task :run
Failed to read in AWS credentials: java.lang.IllegalStateException: System.getenv(resourceName) must not be null
java.lang.IllegalStateException: System.getenv(resourceName) must not be null
	at dartzee.utils.AwsUtils.getAwsCredentialsStr(AwsUtils.kt:30)
	at dartzee.utils.AwsUtils.readCredentials(AwsUtils.kt:15)
	at dartzee.logging.LoggerFactory.constructElasticsearchDestination(LoggerFactory.kt:13)
	at dartzee.utils.InjectedThings.<clinit>(InjectedThings.kt:34)
	at dartzee.main.MainUtilKt.setLoggingContextFields(MainUtil.kt:38)
	at dartzee.main.DartsMainKt.main(DartsMain.kt:24)
Failed to read in AWS credentials: java.lang.IllegalStateException: System.getenv(resourceName) must not be null
2021-03-17 23:30:37   [renderedDartboard]  (447ms) Rendered dartboard[350, 350] in 447ms (cached: false)
java.lang.IllegalStateException: System.getenv(resourceName) must not be null
	at dartzee.utils.AwsUtils.getAwsCredentialsStr(AwsUtils.kt:30)
	at dartzee.utils.AwsUtils.readCredentials(AwsUtils.kt:15)
	at dartzee.utils.AwsUtils.makeS3Client(AwsUtils.kt:35)
	at dartzee.sync.AmazonS3RemoteDatabaseStore.<init>(AmazonS3RemoteDatabaseStore.kt:19)
	at dartzee.utils.InjectedThings.<clinit>(InjectedThings.kt:42)
	at dartzee.main.MainUtilKt.setLoggingContextFields(MainUtil.kt:38)
	at dartzee.main.DartsMainKt.main(DartsMain.kt:24)

Exception: java.lang.NoClassDefFoundError thrown from the UncaughtExceptionHandler in thread "main"

```

Anyway, thought this would be a good way of saying hello! :D

### Backlog
Full backlog can be found [here](https://trello.com/b/Plz8blWw/dartzee)
