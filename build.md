# 手動Build手順

``` sh
./gradlew bootBuildImage --imageName=ablankz/nova-plan-service:1.0.2
docker push ablankz/nova-plan-service:1.0.2
```