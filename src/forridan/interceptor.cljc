(ns forridan.interceptor
  (:require [clojure.spec.alpha :as s]
            [clojure.repl :as repl]))

(s/def ::name string?)

(s/def ::before fn?)
(s/def ::after fn?)

(s/def :forridan/interceptor
  (s/keys :req-un [::name]
          :opt-un [::lens ::before ::after]))

(defn fn-name
  [f]
  #?(:clj
     (as-> (str f) $
       (repl/demunge $)
       (or (re-find #"(.+)--\d+@" $)
           (re-find #"(.+)@" $))
       (last $))
     :cljs
     (as-> (.-name f) $
       (demunge $)
       (s/split $ #"/")
       ((juxt butlast last) $)
       (update $ 0 #(s/join "." %))
       (s/join "/" $))))

(defn get-fnspec
  [f]
  (let [fspec-map (-> (fn-name f)
                      symbol
                      s/get-spec
                      s/describe
                      rest
                      (->> (apply hash-map)))]
    fspec-map))

(defn get-fnspec-keys
  [f]
  (let [fnspec (get-fnspec f)
        args (-> (:args fnspec)
                 pop
                 (->> (partition 2)
                      (map second)
                      (into [])))
        ret (:ret fnspec)]
    [args ret]))

(defn magic-spec-call
  [f context]
  (let [[args ret] (get-fnspec-keys f)
        args (map (partial get context) args)
        result (apply f args)]
    (assoc context ret result)))
