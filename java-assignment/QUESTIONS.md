# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor it to make it consistent. Right now, the Store part directly accesses the database from the REST layer, but the Warehouse part follows a cleaner structure with separate layers. For long-term maintenance, it’s better to follow one consistent pattern. I would prefer the warehouse-style approach because it clearly separates business logic from database code, which makes the code easier to understand, maintain, and test.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches are good in different situations. Using OpenAPI is better for bigger systems because it clearly defines the API contract, keeps documentation updated, and makes it easier for other teams to integrate. Manual coding is quicker and simpler, especially for small internal APIs. Personally, I would use OpenAPI for public or shared APIs, and manual coding for smaller internal services where speed and flexibility are more important.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would focus first on testing the business logic, especially the warehouse use cases, because they contain important validations like capacity and stock rules. I would write unit tests for those scenarios. Then I would add integration tests to make sure the REST APIs work correctly end-to-end. I wouldn’t spend time testing framework code, but instead focus on the actual business behavior. And whenever new features are added, I would make sure proper tests are added along with them
```
