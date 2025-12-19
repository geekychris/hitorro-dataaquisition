# Hitorro Data Acquisition

This is the **hitorro-dataaquisition** project, providing comprehensive data acquisition capabilities for fetching and importing data from various external sources.

## Overview

The Data Acquisition module provides connectors and importers for multiple social media platforms and data sources, enabling automated collection and processing of external data streams.

## Features

### Data Sources

- **Twitter Integration**: Real-time streaming and search via Twitter4J
- **Facebook Integration**: Graph API access via RestFB
- **LinkedIn Integration**: Professional network data acquisition
- **Wikipedia**: Wiki page fetching and parsing
- **Yahoo**: YQL (Yahoo Query Language) integration
- **Generic Fetcher**: Configurable HTTP-based data fetching

### Data Import Capabilities

- **Mapping System**: Flexible data mapping from external formats to internal structures
- **JSON to Bag**: Convert JSON data to internal Bag representation
- **XML to Bag**: Parse and convert XML data structures
- **Twitter Specific**: Twitter status to internal format conversion
- **Wikipedia Specific**: Wiki markup parsing and conversion

### Utility Features

- **Bag I/O**: Frame-based and XML-based Bag persistence
- **Key Frames**: Efficient key-value storage for large datasets
- **SQL-Like Queries**: Simple SQL-like query interface for data filtering

## Project Structure

```
hitorro-dataaquisition/
├── pom.xml
├── README.md
└── src/main/java/com/hitorro/
    ├── dataaquisition/
    │   ├── facebook/         - Facebook API integration
    │   ├── fetcher/          - Generic HTTP fetching services
    │   ├── importing/        - Data import and mapping system
    │   ├── linkedin/         - LinkedIn integration (placeholder)
    │   ├── twitter/          - Twitter API and streaming
    │   ├── wiki/             - Wikipedia page gathering
    │   └── yahoo/            - Yahoo YQL integration
    └── util/
        ├── bagio/            - Bag I/O utilities
        ├── io/keyframes/     - Key-value frame storage
        └── sqllike/          - SQL-like query interface
```

## Dependencies

### Internal Hitorro Dependencies (provided scope)
- `hitorro-analysis` (3.0.0)
- `hitorro-text-persistence` (3.0.0)
- `hitorro-basedms` (3.0.0)
- `hitorro-dedupe` (3.0.0)
- `thirdpartysource` (2.0)

### External Dependencies
- **Twitter4J** (4.1.2) - Twitter API client
- **RestFB** (2023.12.0) - Facebook API client
- **Apache HttpClient** (4.5.14) - HTTP operations
- **Jackson** (2.16.0) - JSON processing
- **JDOM2** (2.0.6.1) - XML processing
- **Trove4j** (3.0.3) - High-performance collections

## Building

### Build the JAR

```bash
cd /Users/chris/hitorro/hitorro-dataaquisition
mvn clean package
```

This will create:
- `target/hitorro-dataaquisition-3.0.0.jar` - Standard JAR
- `target/hitorro-dataaquisition-3.0.0-jar-with-dependencies.jar` - Fat JAR with all dependencies

### Install to Local Repository

```bash
mvn clean install
```

### Build Without Tests

```bash
mvn clean package -DskipTests
```

## Usage Examples

### Twitter Streaming

```java
import com.hitorro.dataaquisition.twitter.TwitterClient;

// Initialize Twitter client with API credentials
TwitterClient client = new TwitterClient(consumerKey, consumerSecret, 
                                         accessToken, accessTokenSecret);

// Start streaming tweets
client.startStreaming(statusListener);
```

### Facebook Data Fetching

```java
import com.hitorro.dataaquisition.facebook.FacebookSearch;

// Fetch Facebook data
FacebookSearch search = new FacebookSearch(accessToken);
List<Post> posts = search.searchPosts(query);
```

### Generic Web Fetching

```java
import com.hitorro.dataaquisition.fetcher.FetcherService;

FetcherService fetcher = new FetcherService();
String content = fetcher.fetch(url);
```

## Configuration

API credentials should be configured via properties files or environment variables:

```properties
# Twitter API
twitter.consumer.key=YOUR_CONSUMER_KEY
twitter.consumer.secret=YOUR_CONSUMER_SECRET
twitter.access.token=YOUR_ACCESS_TOKEN
twitter.access.token.secret=YOUR_ACCESS_TOKEN_SECRET

# Facebook API
facebook.app.id=YOUR_APP_ID
facebook.app.secret=YOUR_APP_SECRET
facebook.access.token=YOUR_ACCESS_TOKEN
```

## Testing

```bash
mvn test
```

## Requirements

- Java 19 or higher
- Maven 3.6+
- Valid API credentials for external services (Twitter, Facebook, etc.)

## Maven Artifact

To use this module in other Maven projects:

```xml
<dependency>
    <groupId>ht</groupId>
    <artifactId>hitorro-dataaquisition</artifactId>
    <version>3.0.0</version>
</dependency>
```

## License

See project root for license information.
