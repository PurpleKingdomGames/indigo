# IndigoDoc Syntax

First attempt at finding a way to document the APIs. It's horrible explicit, repetitive and error prone! :-)

## Format

Indigo docs are all prefixed with any amount of whitespace and a single line comment. The hope is disambiguate between javadoc/scaladoc's and indigodoc.

## Header

All indigodoc's begin with a header, and may contain nothing more than a header.
A header will look like:

`// indigodoc entity:class name:ClearColor`

Parts:

- Opening comment `//`
- `indigodoc` - required - used to tell the parser to read from here.
- `entity` - required - `class|static|value|method|function`
- `name` - required - the name of the thing

For values:

- `type` - optional - used to 
