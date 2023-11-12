
plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

allprojects {
    group = "eu.4fh"
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
	implementation("com.github.spotbugs:spotbugs-annotations:4.8.1")
	implementation("org.json:json:20231013")
	implementation("org.apache.httpcomponents.core5:httpcore5:5.2.3")
    implementation("org.glassfish.jersey.core:jersey-client:3.1.3")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.3")
    api("org.dmfs:oauth2-essentials:0.22.1")
    val httpVersion = "1.21.3"
    implementation("org.dmfs:http-client-essentials:${httpVersion}")
    implementation("org.dmfs:http-client-basics:${httpVersion}")
    implementation("org.dmfs:http-client-headers:${httpVersion}")
    implementation("org.dmfs:http-executor-decorators:${httpVersion}")
    implementation("org.dmfs:http-client-types:${httpVersion}")
    implementation("org.dmfs:httpurlconnection-executor:${httpVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.easymock:easymock:5.0.0")

}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
