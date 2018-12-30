(ns book-report.core
  (:require [clojure.string :as string]
            [clojure.pprint :refer [pprint]]))

(defn format-return
  "Format the return value based on the number of lines.
  Takes a return value string and lines of output.
  Returns string of output lines followed by return value."
  [return-value lines]
  (str
   (when (pos? (count lines))
     "      ")
   return-value))

(defn append
  "Appends an item to a list.
  Takes an optional formatter function, the new item, and a list.
  Suitable for list thread macros.
  Returns a vector with item appended to end of list."
  ([item list]
   (append (fn [x & more] x) item list))
  ([f item list]
   (conj (vec list) (f item list))))

(defn prepend-str
  "Takes a partial string and aggregate string.
  Returns partial string prepended to beginning of aggregate string."
  [substr s]
  (str substr s))

(defn append-str
  "Takes a partial string and aggregate string.
  Returns aggregate string with partial string appended to end."
  [substr s]
  (str s substr))

(defn format-note
  "Align notes vertically.
  Takes a list of strings.
  Returns a list of strings."
  [[note & lines]]
  (->> lines
       (map #(str "      " (string/trim %)))
       (cons note)))

(defn format-notes
  "Takes a list of notes and formats them so that newlines
  are aligned vertically and separate notes are prefixed with a - (dash).
  Returns sequence of clojure forms to print list of notes."
  [notes]
  (->> notes
       (map #(str "    - " (string/trim %)))
       (mapcat #(format-note (string/split % #"\n")))
       (string/join "\n")
       (prepend-str "  Notes:\n")
       (append-str "\n")
       (conj `[println])
       (seq)))

(defn format-code-lines
  "Format code forms as 2-space indented strings.
  Takes a list of forms.
  Returns list of strings."
  [lines]
  (->> lines
       (map #(str "  " %))
       (string/join "\n")))

(defn space-code-blocks
  "Groups code blocks based on line-count. If a code block is a single line then
  space them out with a single line.
  If a code block spans multiple lines, space the code block with two lines.
  Kind of like a conditional (clojure.string/join \"\n\" coll)
  Takes the aggregate code string and the next block of code string.
  Returns the aggregate code string."
  [code-str block]
  (let [line-count (count (string/split block #"\n"))]
    (if (< line-count 2)
      (str code-str block "\n")
      (str code-str block "\n\n"))))

(defn format-code
  "Formats each form as an intented list of strings.
  Takes a list of forms to print.
  Returns a list of clojure forms to print the forms as a single code string."
  [forms]
  (->> forms
       (map #(with-out-str (pprint %)))
       (map string/trim)
       (map #(format-code-lines (string/split % #"\n")))
       (reduce space-code-blocks "")
       (string/trimr)
       (conj `[println])
       (seq)))

(defmacro with-out-str-and-value
  "Similar to with-out-str but returns list of output and return value.
  Takes body expression forms.
  Returns vector [output-str, return-value]."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [v# ~@body]
         [(str s#) v#]))))

(defn run-code
  "Takes a list of forms and wraps it in a list of do commands.
  Takes a list of expression forms
  Returns list of symbols to eval later."
  [forms]
  `(do ~@forms))

(defn eval-str
  "Takes a form expression.
  Returns a list of [output-str, return-value]."
  [form]
  (with-out-str-and-value (eval form)))

(defn format-eval-results
  "Evaluates forms, captures output, and aligns it to the indented eval column.
  Takes a list of form expressions.
  Returns list of clojure forms to print code evaluation results."
  [form]
  (let [[output return-value] (eval-str form)
        [first-line & lines] (string/split-lines output)]
    (->> lines
         (map string/trim)
         (map #(str "      " %))
         (cons first-line)
         (remove empty?)
         (append format-return
                 (pr-str return-value))
         (string/join "\n")
         (prepend-str "   -> ")
         (append-str "\n"))))

(defn format-eval
  "Formats a form to be captured and evaluated later at run time.
  Takes a list of forms to evaluate.
  Returns a list of forms to evaluate and format the output from the forms."
  [forms]
  `(println (format-eval-results '~(run-code forms))))

(defn format-title
  "Display a title indented with 2 spaces and an underline.
  Takes a list of title strings.
  Returns list of clojure forms to print the title."
  [titles]
  (let [title (apply str "  # " titles)
        underline (apply str "\n  " (repeat (count title) "–"))
        title (str title underline "\n")]
    `(println ~title)))

(defn eval-form?
  "Determine if a form is a special case form used by the lesson macro. If it is
  then return false otherwise return true for normal cojure forms.
  Used to group eval statements between notes, title, or run forms
  Takes a clojure form list
  Returns true or false."
  [form]
  (if (seq? form)
    (not (contains? '#{::value notes run title} (first form)))
    true))

(defn format-output
  "Takes a vector of forms.
  Returns a form to run a sequence of forms."
  [forms]
  (let [output (seq forms)]
    `(do ~@output
         (println ""))))

(defn ->seq
  "Checks if form is a sequence otherwise makes a sequence"
  [form]
  (if (seq? form)
      form
      (list ::value form)))

(defn display
  "Takes a vector of forms to evaluate and a list of forms to append.
  Returns a vector of forms with new lists conjoined to the end."
  [output & form-lists]
  (->> form-lists
       (remove empty?)
       (reduce conj (vec output))))

(defn process-internal-form
  "Process a form if it contains an internal form such as notes, run, or title.
  Takes a form like `'(+ 1 2)`
  Returns a list of forms to evaluate."
  [form]
  (let [[form-head & form-args] (->seq form)]
    (case form-head
      notes   (format-notes form-args)
      run     (run-code form-args)
      title   (format-title form-args)
      nil)))

(defn process-standard-forms
  "Handles standard clojure evaluation forms within a lesson.
  This allows the user to specify (lesson 1 \"title\"
                                            (def x 1)
                                            (+ x 1)).
  Takes a list of forms, some may be internal forms.
  Returns a vector of remaining forms to process and updated output."
  [forms output]
  (let [[eval-forms remaining] (split-with eval-form? forms)]
    [remaining (display output
                        (format-code eval-forms)
                        (format-eval eval-forms))]))

(defn build-output
  "Builds up the output to return from the lesson macro.
  Takes a list of forms provided to the lesson.
  Returns a list of remaining forms to process and updated output."
  [[form & remaining :as forms] output]
  (if (eval-form? form)
    (process-standard-forms forms output)
    [remaining (display output
                        (process-internal-form form))]))

(defmacro lesson
  "Render code and its output grouped as a lesson from a chapter.
  Takes a chapter name, title string, notes, and code forms.
  Returns a list of expressions to display it nicely.
  (lesson 1
          \"Calling functions\"
          (title \"Addition example\")
          (notes \"Should return 2\")
          (run (def add +))
          (add 1 1))
  "
  [chapter title & forms]
  (loop [forms forms
         output `[(println ~(str "Chapter " chapter " :: " title "\n"))]]
    (if (empty? forms)
      (format-output output)
      (let [[remaining output] (build-output forms output)]
        (recur remaining output)))))

(comment
  (macroexpand
   '(lesson 1
            "This is a test lesson"
            (notes  "Should return 2")
            (+ 1 1)))

  (lesson 1
          "Lesson title"
          (notes "Note")
          (+ 1 2)
          (title "Title")
          (run (def x 3))
          (+ x 1))

  (lesson 1 "What can the lesson macro do?"
    (title "Overview")
    (notes "Display notes"
           "Run expressions"
           "Evaluate forms"
           "View results")
    (run (def msg "I'm behind the scenes"))
    (println (str msg  ". Now I'm visible!")))

  (lesson 2 "Run Example"
    (run (def x 3)
         (def y 2)))

  (lesson 3 "Title Example"
    (title "Underlined Title")))
