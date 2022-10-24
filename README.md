# abstract-bnet-api

Basis for other bnet-api projects.
Can be included in other projects as source dependency via gradle.

Other projects:<br>
settings.gradle.kts
```
sourceControl {
    gitRepository(uri("https://github.com/FH3095/abstract-bnet-api.git")) {
        producesModule("eu.4fh:abstract-bnet-api")
    }
}
```
build.gradle.kts
```
dependencies {
	implementation("eu.4fh:abstract-bnet-api") {
		version {
			branch = "main"
		}
	}
}
```


To use this in eclipse, this project has to be checked out with the name `abstract-bnet-api`.
