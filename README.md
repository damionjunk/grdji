# grdji

## Running Examples

### Help

`lein run -- -h`

```
grdji record parser

Usage: grdji [options]

Options:
  -c, --config EDNFILE  EDN Config Overrides
  -r, --repl            Start a repl and do nothing.
  -w, --web             Start a REST web server.
  -f, --file INPUT      Input Records File for output or API pre-loading
  -o, --output OPT      Sort option 1: email(desc)last(asc) 2: dob(asc) 3: last(desc)
  -h, --help
```

### Step 1 Output

`lein run -- -f data/sample1.csv -o 1`

### API Server w/REPL

`lein run -- -f data/sample1.csv -r -w`

This preloads the sample data and starts a REPL on port 7884 and an HTTP server on port 3000.

#### Curl Examples:

```
curl -X GET localhost:3000/records/email
curl -X GET localhost:3000/records/name
curl -X GET localhost:3000/records/birthdate
curl -v -d "line=1,2,bee@boo.com,4,1999-03-01" -X POST localhost:3000/records
curl -v -d "line=ln fn bee@boo.com 5 1999-03-01" -X POST localhost:3000/records
curl -v -d "line=lna | fna | bzee@boo.com  | orange  | 2019-03-01" -X POST localhost:3000/records
```

## Inputs

- Pipe-delimited, comma-delimited, or space-delimited file.
  - `LastName, FirstName, Email, FavoriteColor, DateOfBirth`
 - DateOfBirth
   - Input Format is `yyyy-MM-dd`
    - https://www.iso.org/iso-8601-date-and-time-format.html
   - https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
  
## Outputs

1. Sorted by email (descending), then by last name (ascending)
2. Sorted by birth date (ascending)
3. Sorted by last name, descending

Dates are displayed as `M/d/yyyy`
