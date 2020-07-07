(ns asm.parser
  (:require [clojure.string :as str]))

(defn is-register? [x]
  (nil? (re-find #"[0-9]" x)))

(defn get-value [x]
  (if (is-register? x)
    (keyword x)
    (Integer/parseInt (str x))))

(defn parse-msg [instruction]
  (println "instruction: " instruction)
  (into [(keyword (subs instruction 0 3))]
        (let [input (map identity (subs instruction 4))]
          (loop [to-parse input
                 res []
                 in-quote? false
                 current-string ""]
            (let [i (first to-parse)]
              (if (empty? to-parse)
                (conj res (get-value current-string))
                (cond
                  (and in-quote? (= i \')) (recur (rest to-parse) (conj res current-string) false "")
                  (and in-quote? (not= i \')) (recur (rest to-parse) res in-quote? (str current-string (str i)))
                  (and (not in-quote?) (= i \')) (recur (rest to-parse) (if (= "" current-string)
                                                                          res
                                                                          (conj res (get-value current-string))) true "")
                  (and (not in-quote?) (= i \space)) (recur (rest to-parse) res in-quote? current-string)
                  (and (not in-quote?) (= i \;)) (conj res (get-value current-string))
                  :else (recur (rest to-parse) res in-quote? (str current-string (str i))))))))))

(defn to-keywords [instructions]
  (let [[instruction op1 op2] (str/split instructions #" ")]
    (if (= "msg" instruction)
      (parse-msg instructions)
      (cond-> [(keyword instruction)]
              (not (nil? op1)) (conj (get-value op1))
              (not (nil? op2)) (conj (get-value op2))))))

(defn scrub-comments [s]
  (if (and (not (str/starts-with? s "msg"))
           (str/includes? s ";"))
    (str/trimr (subs s 0 (str/index-of s ";")))
    s))

(defn parse [asm]
  (->> (str/split-lines asm)
       (map #(str/trimr (str/triml %)))
       (map scrub-comments)
       (remove #(= "" %))
       (remove #(str/starts-with? % ";"))
       (map to-keywords)))

(clojure.pprint/pprint
  (parse "; my first program
          mov a 5
          inc a     ; increment a
          call foo
          msg '(5+1)/2 = ;' a ; another comment.
          end
          foo:
          div a 2
          ret"))