(ns book-report.core
  (:require [clojure.string :as string]
            [clojure.pprint :refer [pprint]]))

(defn format-return
  [item list]
  (if (> (count list) 1)
    (str "      " item)
    (str item)))

(defn append
  ([item list]
   (append identity item list))
  ([f item list]
   (conj (vec list) (f item list))))

(defn format-note
  [[note & lines]]
  (cons note
        (->> lines
             (map #(str "     " (string/trim %))))))

(defn format-notes
  [notes]
  (->> notes
       (map #(str "   - " (string/trim %)))
       (mapcat #(format-note (string/split % #"\n")))
       (string/join "\n")
       (conj `[println "  Notes:\n"])
       (seq)))

(defn format-code-lines
  [lines]
  (->> lines
       (map #(str "  " %))
       (string/join "\n")))

(defn space-code-blocks
  [code-str block]
  (let [line-count (count (string/split block #"\n"))]
    (if (< line-count 2)
      (str code-str block "\n")
      (str code-str block "\n\n"))))

(defn format-code
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
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [v# ~@body]
         [(str s#) v#]))))

(defn run-code
  [forms]
  `(do ~@forms))

(defn eval-str
  [form]
  (with-out-str-and-value (eval form)))

(defn format-eval
  [forms]
  (let [[output return-value] (eval-str `(do ~@forms))
        [first-line & lines] (string/split output #"\n")]
    (->> lines
         (map string/trim)
         (map #(str "      " %))
         (cons first-line)
         (append format-return (pr-str return-value))
         (remove empty?)
         (string/join "\n")
         (conj `[println "   ➜"])
         (seq))))

(defn format-title
  [titles]
  (let [title (apply str "  # " titles)
        underline (apply str "\n  " (repeat (count title) "–"))
        title (str title underline)]
    `(println ~title)))

(comment
 (eval (format-title "Hello World")))

(defn op?
  [form]
  (if (seq? form)
    (not (contains? '#{::value notes run title} (first form)))
    true))

(defmacro lesson
  "Render code and its output grouped as a lesson from a chapter.
  Takes a section-id number, title string, notes, and code forms.
  Returns a list of expressions to display it nicely.
  (lesson 1
          \"Calling functions\"
          (title \"Addition example\")
          (notes \"Should return 2\")
          (+ 1 1))
  "
  [section-id title & forms]
  (loop [forms forms
         output `[(println (str "Chapter " ~section-id " :: " ~title))]]
    (let [append-line #(apply conj output (conj %& `(println "")))
          form (first forms)
          remaining (rest forms)
          [form-head & form-args] (if (seq? form)
                                    form
                                    (list ::value form))]
      (cond (nil? form) `(do ~@(seq (conj output `(println ""))))
            (= form-head ::value) (recur remaining
                                         (apply append-line form-args))
            (= form-head 'notes)  (recur remaining
                                         (append-line
                                          (format-notes form-args)))
            (= form-head 'run)    (do
                                    (eval `(do ~@form-args))
                                    (recur remaining output))

            (= form-head 'title) (recur remaining
                                        (append-line (format-title form-args)))
            :else
              ;; Scoop up all forms between one for the special case forms
              ;; above
              (let [[eval-forms remaining] (split-with op? forms)]
                (recur remaining
                       (append-line (format-code eval-forms)
                                    (format-eval eval-forms))))))))

(comment
   (macroexpand
    '(lesson 1
             "This is a test lesson"
             (notes  "Should return 2")
             (+ 1 1)))
   (lesson 0
          "Calling functions"
          (title "Add")
          (notes "Should return 2")
          (+ 1 1))
   (lesson 1
           "This is a test lesson"
           (notes  "Should return 2")
           (+ 1 1))
   (lesson 2
           "This is a test lesson"
           (notes "Should return 2")
           (+ 1 1)
           (+ 2 3))
   (lesson 3
           "This is a test lesson"
           (+ 1 1))
   (lesson 4
           "This is a test lesson"
           (notes  "Should return 2
                   Plus another note")
           (+ 1 1))
   (lesson 5
           "This is a test lesson"
           (notes  "Should return 2")
           (+ 1 1))
   (lesson 6
           "This is a test lesson"
           (notes "Should return 2")
           (+ 1 3)
           (notes "x")
           2)
   (lesson 7
           "This is a test lesson"
           (notes "Should return 2")
           (run (defn add [x] (+ x 3)))
           (notes "x")
           (add 3))
   (lesson 8
           "This is a test lesson"
           (notes "Should return 2")
           (run (defn add [x] (+ x 3)))
           (title "Look at x")
           (notes "x")
           (add 3)))
