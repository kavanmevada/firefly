# FireFly!
FireFly is server application which provides **Database** and **Security** for REST API.

### Database
Uses **NoSQL** data model, you store data in documents that contain fields mapping to values. These documents are stored in collections, which are containers for your documents that you can use to organize your data and build queries. Documents support many different data types, from simple strings and numbers, to complex, nested objects. You can also create sub-collections within documents and build hierarchical data structures that scale as your database grows.

Additionally, reading and writing data is efficient, and flexible. Retrieving data at the document level without needing to retrieve the entire collection, or any nested sub-collections and also has compatibility of retrieving data at sub-collection.

### Security
Security Rules stand between your data and malicious users. You can write simple or complex rules that protect your app's data to the level of granularity that your specific requirements. Security Rules to define what data your users can access for Database.

Security Rules work by matching a pattern against database paths, and then applying custom conditions to allow access to data at those paths. A path-matching component and a conditional statement allowing read or write access. You must define Rules for application.

## Security Rules

### Match

A  `match`  block declares a  `path`  pattern that is matched against the path for the requested operation (the incoming  `request.path`). The body of the  `match`  must have one or more nested  `match`  blocks,  `allow`  statements, or  `function`  declarations. The path in nested  `match`  blocks is relative to the path in the parent  `match`  block.

```js
// Given request.path == /example/hello/nested/path the following
// declarations indicate whether they are a partial or complete match and
// the value of any variables visible within the scope.
// Partial match.
match /example/{singleSegment} {   // `singleSegment` == 'hello'
  allow write;                     // Write rule not evaluated.
  // Complete match.
  match /nested/path {             // `singleSegment` visible in scope.
    allow read;                    // Read rule is evaluated.
  }
}

// Complete match.
match /example/{multiSegment=**} { // `multiSegment` == /hello/nested/path
  allow read;                      // Read rule is evaluated.
}
```

The  `path`  pattern is a directory-like name that may include variables or wildcards. The  `path`  pattern allows for single-path segment and multi-path segment matches. Any variables bound in a  `path`  are visible within the  `match`  scope or any nested scope where the  `path`  is declared.

As the example above shows, the  `path`  declarations supports the following variables:

-   **Single-segment wildcard:**  A wildcard variable is declared in a path by wrapping a variable in curly braces:  `{variable}`. This variable is accessible within the  `match`  statement as a  `string`.
-   **Recursive wildcard:**  The recursive, or multi-segment, wildcard matches multiple path segments at or below a path. This wildcard matches all paths below the location you set it to. You can declare it by adding the  `=**`  string at the end of your segment variable:  `{variable=**}`. This variable is accessible within the  `match`  statement as a  `path`  object.

### Allow

The  `match`  block contains one or more  `allow`  statements. These are your actual rules. You can apply  `allow`  rules to one or more methods. The conditions on an  `allow`  statement must evaluate to true for Cloud Firestore or Storage to grant any incoming request. You can also write  `allow`  statements without conditions, for example,  `allow read`. If the  `allow`  statement doesn't include a condition, however, it always allows the request for that method.

If any of the  `allow`  rules for the method are satisfied, the request is allowed. Additionally, if a broader rule grants access, Rules grant access and ignore any more granular rules that might limit access.

### Conditional operators

|Operator        |Description                    |Associativity    |
|----------------|-------------------------------|-----------------|
|a==b a!=b       |Comparison operators           |left to right    |


## GNU General Public License v3.0
Permissions of this strong copyleft license are conditioned on making available complete source code of licensed works and modifications, which include larger works using a licensed work, under the same license. Copyright and license notices must be preserved. Contributors provide an express grant of patent rights.
