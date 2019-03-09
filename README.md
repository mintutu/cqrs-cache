CQRS Cache  
===============  
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f61788b530bc4898a291ed6049963a90)](https://app.codacy.com/app/specterbn/cqrs-cache?utm_source=github.com&utm_medium=referral&utm_content=specterbn/cqrs-cache&utm_campaign=Badge_Grade_Dashboard)
[![CircleCI](https://circleci.com/gh/specterbn/cqrs-cache.svg?style=svg)](https://circleci.com/gh/specterbn/cqrs-cache)
[![codecov](https://codecov.io/gh/specterbn/cqrs-cache/branch/master/graph/badge.svg)](https://codecov.io/gh/specterbn/cqrs-cache)
## Introduction  
Build REST API with CQRS (Command Query Responsibility Segregation) architecture to store key-value in local memory cache.

## Use cases
*   POST /cache/add
*   POST /cache/remove
*   GET /cache/peek
*   POST /cache/take

Get how many request send to cache in interval time by ip-address. Interval time is configured by `rate-schedule` in `application.conf` 
*   GET /cache/rate?ipAddress=

Body format:  
```sh  
{  
    "key" : string,  
    "value" : string  
}  
```  
For example:  
```json  
{  
    "key":"01234567-9abc-def0-1124-56789abc1004",  
    "value":"1234"  
}  
```  
## Deployment
This project is deployed in Heroku: <https://cqrs-cache.herokuapp.com/>

## Technologies  
*   Play framework 2.6  
*   Akka framework 2.5.6

## Design  
**Architecture**

Using CQRS
```text
           add/remove/take/peek  ┌────────────────┐       ┌──────────────────┐
                           ┌───▶ │ CommandService │ ─────▶│ RawInMemoryActor │
                           │     └────────────────┘       └──────────────────┘ 
                           │              |
     ┌─────────────────┐   │              |        forward
──▶  │ CacheController │──▶│              └--------------------------┐                 
     └─────────────────┘   │                                         ▼
                           │     ┌──────────────┐         ┌────────────────────────┐
                           └───▶ │ QueryService │ ──────▶ │ AggregateInMemoryActor │
            rate/rate-report     └──────────────┘         └────────────────────────┘

```

**Data structure**  

The data structure in the project based on MRU (Most Recently Used) cache and used the LinkedMap to implement the
key-value memory cache. LinkedMap keeps track of the order in which each element is added, so the complexity of
Add/Remove/Peek/Take is O(c) (constant time).

## Running  
```sh  
sbt run
```  
