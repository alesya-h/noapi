# NoAPI

Library to expose clojure namespaces as http api.

Namespaces are exposed via middleware, which is
constructed by supplying prefix to filter namespaces
and route prefix under which namespaces are
exposed.

Example usage:
```
(noapi.middleware/wrap-api app "clojure.core" "/clj")
```

`app` is a ring handler to wrap.

Example translation table for `(noapi.middleware/wrap-api app "myapp.my-api" "/api")`:

|  Method  |  URL            |  Form params  |  Function call           |
| -------- | --------------- | ------------- | ------------------------ |
| GET      |  /api/foo?id=1  |               | (myapp.my-api/foo :id 1) |
| POST     |  /api/foo       |               | (myapp.my-api/foo!)      |
| GET      |  /api/foo/bar   |               | (myapp.my-api.foo/bar)   |
| POST     |  /api/foo/bar   |               | (myapp.my-api.foo/bar!)  |
