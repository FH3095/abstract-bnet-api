
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
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
	implementation("com.github.spotbugs:spotbugs-annotations:4.7.3")
	implementation("org.json:json:20220924")
	implementation("org.apache.httpcomponents.core5:httpcore5:5.1.4")
    implementation("org.glassfish.jersey.core:jersey-client:2.37")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:2.37")
    implementation("org.dmfs:oauth2-essentials:0.18")
    implementation("org.dmfs:httpurlconnection-executor:0.20")
	compileOnly("javax.servlet:javax.servlet-api:4.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.easymock:easymock:5.0.0")

}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
