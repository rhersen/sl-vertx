# Vert.x backend/proxy for trafiklab.se

add FileService and realtimedepartures keys to keys.json and then run

    vertx runmod com.mycompany~my-module~1.0.0-final -cp build/mods/com.mycompany~my-module~1.0.0-final -conf keys.json

or

    ./gradlew assemble && vertx runzip build/libs/sl-0.0.1.zip -conf keys.json
