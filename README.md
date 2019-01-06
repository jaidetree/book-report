<p align="center">
  <img src="./doc/book_report_logo.svg" width="700" alt="Book Report Logo" />
</p>

# Book Report

[![Clojars Project][clojars-shield]][clojars-url] [![Build Status][travis-shield]][travis-url]

![Book Report in Atom Screenshot][screenshot]

Book Report provides a Clojure macro to turn Clojure files into an interactive notebook to organize your learning, making it easier to revisit projects without losing context.

```clojure
(lesson 1 "What does a lesson look like?"
  (title "Lesson Introduction")
  (notes "Organize your clojure files like this"
         "Standard clojure forms will be displayed
         and evaluation results shown below")
  (+ 1 2))
```

```
Chapter 1 :: What does a lesson look like?

  # Lesson Introduction
  –––––––––––––––––––––––

  Notes:
    - Organize your clojure files like this
    - Standard clojure forms will be displayed
      and evaluation results shown below

  (+ 1 2)
   -> 3
```

## :star: Features

- Lesson macro helps structure your learning into personal notebook files.
- Revisit projects later by recording your thought process and observations.
- Run lessons from the REPL or a file.
- Distribute repos using the lesson macro to showcase an API.
- Not dependent on any REPL tooling or specific Clojure environment.
- Avoid having to maintain eval results in your code files.

## :question: Why

Going through Daniel Higginbotham's [Clojure for the Brave and True][brave-true] myself I wanted something notebook-like to provide structure so that I can revisit later and not lose context about what I wrote or what example code does.

I tried many existing notebook solutions but there was always some wall where a Clojure feature was not supported.

## :rocket: Installation

[![Book Report on Clojars][clojars-svg]][clojars-url]

Leiningen users add the coords to your `project.clj` or `profiles.clj`:

```clojure
[book-report "0.1.0"]
```

Clojure tools.deps users add the coords to your `deps.edn`:

```clojure
{:deps {book-report {:mvn/version "0.1.0"}}}
```

## :flashlight: Usage

```clojure
(ns some-book.chapter-1
  (:require [book-report.core :refer [lesson]]))

(lesson 1 "What can the lesson macro do?"
  (title "Overview")
  (notes "Display notes"
         "Run expressions"
         "Evaluate forms"
         "View results")
  (run (def msg "I'm behind the scenes"))
  (println (str msg  " but now I'm visible!")))
```

Which prints:

```
Chapter 1 :: What can the lesson macro do?

  # Overview
  ––––––––––––

  Notes:
    - Display notes
    - Run expressions
    - Evaluate forms
    - View results

  (println (str msg " but now I'm visible!"))
   -> I'm behind the scenes but now I'm visible!
      nil
```

If you save that snippet as a file you can always revisit it by running it with `clj` or `lein run`.

## :electric_plug: API

```
(lesson [chapter title & body])
```
_A variadic macro to print code and evaluations as a lesson._

**chapter** _string_ — Chapter identifier like `"1.1.1"` or `1.2`.

**title** _string_ — Section title string

**& body** _seq_ — Clojure form sequences including special forms documented below.

Example: `(+ 1 2)`

## :nut_and_bolt: Special Forms

Some forms in the body are handled differently within the body of the lesson macro.

**(notes [note-str & notes])**

Displays a list of notes. New lines will be indented.

```clojure
(lesson 1 "Notes Example"
  (notes "look like this\nand this"
         "supports new lines"
         "variadic function"))
;; =>
Chapter 1 :: Notes Example

 Notes:
   - look like this
     and this
   - supports new lines
   - variadic function


```

**(run [& forms])**

```clojure
(lesson 2 "Run Example"
  (run (def x 3)
       (def y 2)))
;; =>
Chapter 2 :: Run Example


```

**(title [title-str])**

```clojure
(lesson 3 "Title Example"
  (title "Underlined title"))

;; =>
Chapter 3 :: Title Example

  # Underlined Title
  ––––––––––––––––––––


```

## :white_check_mark: What Next?

- [ ] Allow custom special forms and handlers
- [ ] ClojureScript support

Please let me know if either of these features would help, or any other ideas how I can improve this library :smile:

## :rotating_light: Known Issues

It's unknown how all code will behave within a lesson wrapper. If you run into any problems, errors, or unexpected behaviors please create an issue in this repo.

## :bulb: Contributing

If you would like to contribute please create a pull request and explain what the problem is, and how your solution solves the problem.

I'm also open to new features, refinements, and code-review. Clojure is still relatively new to me and I know the source can be improved.

## License

Copyright © 2018 Jay Zawrotny

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

[brave-true]: https://www.braveclojure.com/clojure-for-the-brave-and-true/
[clojars-shield]: https://img.shields.io/clojars/v/book-report.svg
[clojars-url]: https://clojars.org/book-report
[clojars-svg]: https://clojars.org/book-report/latest-version.svg
[travis-shield]: https://travis-ci.com/eccentric-j/book-report.svg?branch=master
[travis-url]: https://travis-ci.com/eccentric-j/book-report
[screenshot]: ./doc/screenshot.png
