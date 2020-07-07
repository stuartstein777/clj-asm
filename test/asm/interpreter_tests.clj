(ns asm.interpreter-tests
  (:require [clojure.test :refer :all]
            [asm.interpreter :refer :all]))

(deftest mov-tests
  (is (= {:a 5}       (mov {} :a 5)))
  (is (= {:a 5 :b 5}  (mov {:b 5} :a :b))))

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
                                         [:inc :a]])))
  (testing "nops"
    (is (= {:a 6, :b 5, :c 5} (interpret [[:mov :a 5]
                                          [:inc :a]
                                          [:mov :b :a]
                                          [:dec :b]
                                          [:mul :a :b]
                                          [:mov :c :b]
                                          [:nop]
                                          [:nop]
                                          [:label :foo]
                                          [:add :a :c]
                                          [:sub :a :b]
                                          [:div :a :c]
                                          [:end]
                                          [:inc :a]]))))
  (is (= {:a 1} (interpret [[:mov :a 0]
                            [:inc :a]
                            [:inc :a]
                            [:jmp :foo]
                            [:inc :a]
                            [:inc :a]
                            [:label :foo]
                            [:dec :a]])))

  (is (= {:a 0 :b 2 :c 1} (interpret '([:mov :a 0]
                                       [:inc :a]
                                       [:inc :a]
                                       [:jmp :foo]
                                       [:inc :a]
                                       [:nop]
                                       [:nop]
                                       [:label :bar]
                                       [:mov :b 2]
                                       [:mul :a :b]
                                       [:jmp :quax]
                                       [:nop]
                                       [:nop]
                                       [:inc :a]
                                       [:label :foo]
                                       [:dec :a]
                                       [:jmp :bar]
                                       [:label :quax]
                                       [:mov :c 0]
                                       [:inc :c]
                                       [:dec :a]
                                       [:jnz :a -1]
                                       [:end])))))