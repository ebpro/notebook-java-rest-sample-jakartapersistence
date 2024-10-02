# A Not So Simple JAX-RS Example

## Project Description

This project is a sample JAX-RS application demonstrating a library management system. It includes features for managing authors and books, with support for sorting, filtering, and pagination.

## Prerequisites

- Java 21 or higher
- Git

## Installation

1. Clone the repository:
    ```shell
    git clone https://github.com/ebpro/cours-java-librarymanager-rest.git
    cd cours-java-librarymanager-rest
    ```

2. Compile, package, and run integration tests:
    ```shell
    ./mvnw clean verify
    ```

3. Launch the REST server:
    ```shell
    ./mvnw exec:java
    ```

## Usage

### Get a Hello Message

To get a hello message, run the following command:
```shell
curl -s -D - http://localhost:9998/myapp/biblio
```

### Sample Requests

See a complete set of sample requests [here](queries/sample-requests.rest).

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes.
4. Commit your changes (`git commit -m 'Add some feature'`).
5. Push to the branch (`git push origin feature-branch`).
6. Open a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
```
