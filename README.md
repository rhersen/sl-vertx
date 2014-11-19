# Vert.x backend/proxy for trafiklab.se

add FileService and realtimedepartures keys to conf.json and then run

    vertx runmod com.mycompany~my-module~1.0.0-final -cp build/mods/com.mycompany~my-module~1.0.0-final -conf conf.json

or

    ./gradlew assemble && vertx runzip build/libs/my-module-1.0.0-final.zip -conf keys.json
