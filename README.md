# ![Book Report Logo][book-report-logo] Book Report

[![Clojars Project][clojars-shield]][clojars-url] [![Build Status][travis-shield]][travis-url]

![Book Report in Atom Screenshot][screenshot]

Book report provides a Clojure macro to format code into lessons. This library is most suitable for people creating repos to take notes, try examples, and solve exercise problems along with a Clojure book.

Lessons can be run within a file or individually within a REPL.

## Why

Going through Daniel Higginbotham's [Clojure for the Brave and True][brave-true] myself I wanted something notebook-like to comment on behavior that would show the code and the evaluation results. This makes it easier to come back to a chapter and see where I left off along with revisiting previous chapter notes to see a running summary of the ideas.

## Installation

[![Book Report on Clojars][clojars-svg]][clojars-url]

Leiningen users add the coords to your `project.clj` or `profiles.clj`:

```clojure
[book-report "0.1.0"]
```

Clojure tools.deps users add the coords to your `deps.edn`:

```clojure
{:deps {book-report {:mvn/version "0.1.0"}}}
```

## Usage

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
  (println (str msg  " but I'm visible!")))
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

  (println (str msg "but now I'm visible!"))
   -> I'm behind the scenes. Now I'm visible!
      nil
```

## API

```
(lesson chapter title & body)
```

## Special Forms

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
[travis-shield]: https://travis-ci.com/jayzawrotny/book-report.svg?branch=master
[travis-url]: https://travis-ci.com/jayzawrotny/book-report
[book-report-logo]: https://
[screenshot]: ./doc/screenshot.png
