# 手動Build手順

``` sh
./gradlew bootBuildImage --imageName=ablankz/nova-plan-service:1.0.0
docker push ablankz/nova-plan-service:1.0.0
```