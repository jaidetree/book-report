(ns book-report.core-test
  (:require [clojure.test :refer :all]
            [book-report.core :refer :all]
            [clojure.string :refer [split]]
            [clojure.pprint :refer [pprint]]))

(deftest format-return-test
  (testing "format-return"
    (testing "Should return a string with no indentation with 1 or less line"
      (is (= "[1 2 3]"
             (format-return [1 2 3] [])))
      (is (= "[1 2 3]"
             (format-return [1 2 3] ["line"]))))
    (testing "Should return a string indented with 2 or more lines"
      (is (= "      [1 2 3]"
             (format-return [1 2 3] ["line 1" "line 2"]))))))

(deftest append-test
  (testing "append"
    (testing "Should append an item to a list without formatter function"
      (is (= [1 2 3 4]
             (append 4 [1 2 3]))))
    (testing "Should append an item to a list with formatter function"
      (is (= [1 2 3 4]
             (append (fn [v _] (inc v)) 3 [1 2 3]))))))

(deftest append-str-test
  (testing "append-str"
    (testing "Should append a substr to the end of a string"
      (is (= "hello world"
             (append-str " world" "hello"))))))

(deftest prepend-str-test
  (testing "prepend-str"
    (testing "Should prepend a substr to the end of a string"
      (is (= "hello world"
             (prepend-str "hello " "world"))))))

(deftest format-note-test
 (testing "format-note"
   (testing "Should append indented lines to a note str"
     (is (= ["hello" "     world"]
            (format-note ["hello" "world"]))))))

(deftest format-notes-test
  (testing "format-notes"
    (testing "Should return a list of clojure expressions to print notes"
      (is (= `(println "  Notes:\n"
                       "   - hello\n   - world\n     again.\n")
             (format-notes ["hello" "world\nagain."]))))))

(deftest format-code-lines-test
  (testing "format-code-lines"
    (testing "Should show forms as 2-space indented strings."
      (is (= "  hello\n  world"
             (format-code-lines ["hello" "world"]))))))

(deftest space-code-blocks-test
  (testing "space-code-blocks"
    (testing "Should join single-line groups with a single new line"
      (is (= "hello world\n"
             (space-code-blocks "" "hello world"))))
    (testing "Should join mlti-line groups with 2 new lines"
     (is (= "hello\nworld\n\n"
            (space-code-blocks "" "hello\nworld"))))))

(deftest format-code-test
  (testing "format-code"
    (testing "Should take a list of forms and return a list of forms"
      (is (= `(println "  (+ 1 2)")
             (format-code '((+ 1 2))))))))

(deftest with-out-str-and-value-test
  (testing "with-out-str-and-value"
    (testing "Should capture text output and return a value"
      (is (= ["hello world\n" 3]
             (with-out-str-and-value
               (do (println "hello world")
                   (+ 1 2))))))))

(deftest run-code-test
  (testing "run-code"
    (testing "Should return a list of forms in a do block"
      (is (= '(do (println "hello world")
                  (+ 1 2))
              (run-code '((println "hello world") (+ 1 2))))))))

(deftest eval-str-test
  (testing "eval-str"
    (testing "Should evaluate a code form and return the output and results"
      (is (= ["hello world\n" 3]
             (eval-str '(do (println "hello world") (+ 1 2))))))))

(deftest format-eval-results-test
  (testing "format-eval-results"
    (testing "Should evaluate forms and format the output as indented lines"
      (is (= "   ➜ hello world\n      3\n"
             (format-eval-results '(do (println "hello world") (+ 1 2))))))
    (testing "Should format lines without any output"
      (is (= "   ➜ 3\n"
            (format-eval-results '(do (+ 1 2))))))))

(deftest format-eval-test
  (testing "format-eval-test"
    (testing "Should format forms to print the formatted evaluation results"
      (is (= `(println (format-eval-results ~''(do (+ 1 2))))
             (format-eval '((+ 1 2))))))))

(deftest format-title-test
  (testing "format-title"
    (testing "Should return a list of forms to print an underlined title"
      (is (= `(println "  # Title\n  –––––––––\n")
             (format-title "Title"))))))

(deftest eval-form-test
  (testing "eval-form"
    (testing "Should return true if form should be evaluated"
      (is (= '(true true true true true true)
             (map eval-form? '((+ 1 2) (println "hello") 5 "hi" :test {:pass true})))))
    (testing "Should return false if case form should not be evaluted"
      (is (= [false false false false]
             (map eval-form? '((:book-report.core/value) (notes) (run) (title))))))))

(deftest format-output-test
  (testing "format-output"
    (testing "Should return a list of forms wrapped in a do form"
      (is (= `(do (println "hello world") 2 (println ""))
             (format-output `((println "hello world") 2)))))))

(deftest ->seq-test
  (testing "->seq"
    (testing "Should return a form if sequence"
      (is (= '(+ 1 2)
             (->seq '(+ 1 2)))))
    (testing "Should return a form if not a sequence"
      (is (= '(:book-report.core/value 2)
             (->seq 2))))))

(deftest process-internal-form-test
  (testing "process-internal-form"
    (testing "Should return list of forms to display notes"
      (is (= `(println "  Notes:\n" "   - Note\n")
             (process-internal-form '(notes "Note")))))
    (testing "Should return list of forms to run code"
      (is (= `(do (def ~'x 42))
             (process-internal-form '(run (def x 42))))))
    (testing "Should return list of forms to display a title"
      (is (= `(println "  # Title\n  –––––––––\n")
             (process-internal-form '(title "Title")))))
    (testing "Should return nil on normal forms"
      (is (= nil
             (process-internal-form '(+ 1 2)))))))

(deftest process-standard-forms-test
  (testing "process-standard-forms"
    (testing "Should return list of remaining forms and output")))


(deftest display-test
  (testing "display"
    (testing "Should append list of forms to output vector"
      (is (= `[(println "hello world")]
             (display [] `(println "hello world")))))))

(defn capture-lesson
  []
  (-> (with-out-str
        (eval '(book-report.core/lesson
                 1 "Lesson title"
                 (notes "Note")
                 (+ 1 2)
                 (title "Title")
                 (run (def x 3))
                 (+ x 1))))
      (split #"\n")))

(deftest lesson-test
  (testing "lesson"
    (testing "Should output a formatted lesson to stdout"
      (is (= ["Chapter 1 :: Lesson title"
              ""
              "  Notes:"
              "    - Note"
              ""
              "  (+ 1 2)"
              "   ➜ 3"
              ""
              "  # Title"
              "  –––––––––"
              ""
              "  (+ x 1)"
              "   ➜ 4"]
            (capture-lesson))))))
