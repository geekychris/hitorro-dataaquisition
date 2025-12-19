# Hitorro Data Acquisition Extraction Notes

## Extraction Date
December 19, 2025

## What Was Done

This module was extracted from `hitorro-parent/hitorro-dataaquisition` into a standalone Maven project at `/Users/chris/hitorro/hitorro-dataaquisition`.

## Changes Made

1. **Removed parent POM reference**: No longer inherits from hitorro-parent
2. **Made self-contained**: Added all necessary Maven properties and plugin configurations
3. **Updated Java version**: Now using Java 19 (from previous configuration)
4. **Enhanced dependencies**:
   - Updated HTTP Client to 4.5.14
   - Added Twitter4J (4.1.2) for Twitter integration
   - Added RestFB (2023.12.0) for Facebook integration
   - Added Jackson (2.16.0) for JSON processing
   - Added JDOM2 (2.0.6.1) for XML processing
   - Kept Trove4j at 3.0.3
5. **Marked internal dependencies as provided**:
   - hitorro-analysis
   - hitorro-text-persistence
   - hitorro-basedms
   - hitorro-dedupe
   - thirdpartysource
6. **Added JAR building capability**:
   - maven-jar-plugin for standard JAR
   - maven-assembly-plugin for fat JAR with dependencies
7. **Plugin versions updated**:
   - maven-compiler-plugin: 3.11.0
   - maven-jar-plugin: 3.3.0
   - maven-assembly-plugin: 3.6.0
   - maven-surefire-plugin: 3.2.3

## Project Structure

```
hitorro-dataaquisition/
├── pom.xml
├── README.md
├── .gitignore
├── EXTRACTION_NOTES.md
└── src/main/java/com/hitorro/
    ├── dataaquisition/
    │   ├── facebook/
    │   │   ├── FacebookSearch.java
    │   │   ├── Fql.java
    │   │   └── TestFacebookStream.java
    │   ├── fetcher/
    │   │   ├── FetchServiceHandler.java
    │   │   └── FetcherService.java
    │   ├── importing/
    │   │   ├── Importer.java
    │   │   ├── ImporterServiceHandler.java
    │   │   ├── mapping/         - Data mapping system
    │   │   ├── twitter/         - Twitter-specific importers
    │   │   └── wikipedia/       - Wikipedia-specific importers
    │   ├── linkedin/            - LinkedIn integration (placeholder)
    │   ├── twitter/
    │   │   ├── TwitterClient.java
    │   │   ├── TwitterFetchHandler.java
    │   │   ├── TwitterUtil.java
    │   │   ├── StreamHandler.java
    │   │   └── TestTwitterStream.java
    │   ├── wiki/
    │   │   ├── WikiGatherer.java
    │   │   ├── WikiGathererListener.java
    │   │   └── WikipediaDict.java
    │   └── yahoo/
    │       └── Yql.java
    └── util/
        ├── bagio/               - Bag I/O utilities (11 files)
        ├── io/keyframes/        - Key-value frame storage (10 files)
        └── sqllike/             - SQL-like queries (1 file)
```

**Total: 53 Java source files**

## Key Components

### Social Media Integration
- **Twitter**: Real-time streaming, search, and data fetching
- **Facebook**: Graph API integration for posts and user data
- **LinkedIn**: Foundation for professional network integration
- **Yahoo**: YQL query interface

### Data Import System
The importing subsystem provides:
- Generic `Importer` interface
- Tuple-based mapping system (JSON and XML)
- Twitter-specific conversion to internal Bag format
- Wikipedia markup parsing and conversion

### Utility Components
- **Bag I/O**: Frame-based and XML serialization for Bag objects
- **Key Frames**: Efficient key-value storage with Slab-based implementation
- **SQL-Like**: Simple query interface for data filtering

## Dependencies

### Internal Hitorro Dependencies (provided scope)
These must be available at runtime:

1. **hitorro-analysis** (3.0.0) - Text analysis capabilities
2. **hitorro-text-persistence** (3.0.0) - Text indexing and persistence
3. **hitorro-basedms** (3.0.0) - Database and document management
4. **hitorro-dedupe** (3.0.0) - Deduplication utilities
5. **thirdpartysource** (2.0) - Third-party integrations

### External Dependencies (compile scope)
- Twitter4J (4.1.2) - Twitter API client
- RestFB (2023.12.0) - Facebook API client
- Apache HttpClient (4.5.14) - HTTP operations
- Jackson (2.16.0) - JSON processing
- JDOM2 (2.0.6.1) - XML processing
- Trove4j (3.0.3) - High-performance collections

## Build Instructions

### Standard Build
```bash
cd /Users/chris/hitorro/hitorro-dataaquisition
mvn clean package
```

This produces:
- `target/hitorro-dataaquisition-3.0.0.jar` - Standard JAR
- `target/hitorro-dataaquisition-3.0.0-jar-with-dependencies.jar` - Fat JAR

### Install Locally
```bash
mvn clean install
```

### Build Without Tests
```bash
mvn clean package -DskipTests
```

## Configuration Requirements

This module requires API credentials for external services. Create a configuration file or use environment variables:

```properties
# Twitter API Credentials
twitter.consumer.key=YOUR_KEY
twitter.consumer.secret=YOUR_SECRET
twitter.access.token=YOUR_TOKEN
twitter.access.token.secret=YOUR_TOKEN_SECRET

# Facebook API Credentials
facebook.app.id=YOUR_APP_ID
facebook.app.secret=YOUR_SECRET
facebook.access.token=YOUR_TOKEN
```

## Security Notes

⚠️ **Important**: This module handles API credentials and tokens. Never commit credentials to version control!

The `.gitignore` file is configured to exclude:
- `*.properties` files (except those in `src/main/resources/`)
- `credentials.json`
- `api-keys.json`

## Migration Notes

- All internal Hitorro dependencies are marked as `provided` scope
- You must ensure all provided dependencies are available in the runtime classpath
- API credentials must be configured before using social media integrations
- The fat JAR includes all external dependencies but not the provided Hitorro modules

## Next Steps

1. **Build the project**: `mvn clean package`
2. **Configure API credentials**: Set up properties files for Twitter, Facebook, etc.
3. **Build dependencies**: Ensure all provided Hitorro modules are available
4. **Test integrations**: Verify API connections work correctly
5. **Deploy**: Use the fat JAR for standalone deployment or the standard JAR for integrated systems

## Known Issues

- LinkedIn integration is a placeholder and not fully implemented
- Requires valid API credentials for social media services
- Some Twitter API endpoints may have changed since Twitter4J 4.1.2
- Facebook Graph API versions should be verified for compatibility

## Usage in Other Projects

To use this module in another Maven project:

```xml
<dependency>
    <groupId>ht</groupId>
    <artifactId>hitorro-dataaquisition</artifactId>
    <version>3.0.0</version>
</dependency>
```
