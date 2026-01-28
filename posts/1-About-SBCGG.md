Today I start a series of posts about my project called SBCGG (Spring Boot Cloud GraphQL gRPC).

https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

As a developer, we understand the value of active communities in our day-by-day activities. Given something back
to the developers out there has always been part of my career plans and this project was built with this mentality.

As a backend developer, working for multiple companies and different types of business, mostly focused on projects in
the JVM world, I have experimented a lot of different technologies and infrastructure, from monoliths running heavy JSF
stateful platforms to cloud-native microservices.

With the experiences accumulated during the years, some time ago I decided to consolidate my learnings and new studies
in a single project, which ideally would be reusable for my own stuff and side projects. On that direction, the first
thing was to identify a common real-life need to be implemented as a base use case. Then I came up with a user, signing
up to an app, and receiving a message with an authorization code to validate and enable the account.

With the use case defined, the next step was the design and architecture of the system. As a Spring enthusiast,
everything started on top of Spring Boot. The initial versions were pretty rudimentary, not following any clean
architecture standards nor microservices infrastructure.
During the progress of the project, a lot of things were added to it. It started with Maven, but as I've never used
Gradle in a production project before, I switched it to Gradle. Then entered in the microservices world by splitting the
monolith into a microservice to handle users logic and a microservice to store and send the messages.

As part of the recent history of SBCGG, I ended up replacing REST and adopting gRPC for inter service communication and
GraphQL as part of the API gateway.
These two technologies are definitely game changer. Many large-scale IT companies adopted it and are still using in
their core systems.

This is what AI brings in 2026 as a summary for it:

```
Companies using a combination of GraphQL, gRPC, and Spring Boot for their core systems frequently include large-scale
technology firms, particularly those focusing on high-performance microservices, streaming, and API efficiency. Netflix
is a primary example, using gRPC for internal service communication, GraphQL for data delivery and Spring Boot for its
core Java framework.
Other companies leveraging these technologies in their ecosystem include:

* Uber: Uses gRPC for real-time location tracking and backend services.
* Spotify: Employs gRPC for high-performance service communication.
* Airbnb: Uses both gRPC and GraphQL to power their platforms.
* Square: Implements gRPC for payment processing.
* Shopify: Leverages GraphQL for its backend services.
* TikTok: Utilizes gRPC for recommendation engine services.

These organizations often use Spring Boot to manage Java-based microservices, gRPC for fast, low-latency, inter-service
communication, and GraphQL for flexible API consumption by client applications. 
```

After all those interactions with the project, I felt it was in a good shape, but still a big mess to be shared
publicly.
Entering now in the AI era, lazy developers can be extremely happy and more productive by getting AI to organize the
docs and help. That for sure encouraged me a lot to start writing posts and share this project.

With all that said, if you are interested, I now invite you to access the project. As next steps, I am planning to
create more posts, focusing in specific parts of the project. These are some of the topics I have in mind:

* Why GraphQL API resolvers integrated gRPC microservices are so great?
* How Spring Boot 4 simplified observability and why it so important to have metrics and traces exposed in a
  microservices environment?
* Hexagonal Architecture for microservices projects in production.

Please let me know if you have any feedback and please share if you think this can be helpful to other developers out
there.

https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

Thank you.