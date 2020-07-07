(ns asm.interpreter-tests
  (:require [clojure.test :refer :all]
            [asm.interpreter :refer :all]))

(deftest mov-tests
  (is (= {:a 5}       (asm-mov {} :a 5)))
  (is (= {:a 5 :b 5}  (asm-mov {:b 5} :a :b))))

(deftest interpret-tests
  (is (= {:a 6, :b 5, :c 5} (interpret [[:mov :a 5]
                                         [:inc :a]
                                         [:mov :b :a]
                                         [:dec :b]
                                         [:mul :a :b]
                                         [:mov :c :b]
                                         [:add :a :c]
                                         [:sub :a :b]
                                         [:div :a :c]
                                         [:end]
                                         [:inc :a]]))))