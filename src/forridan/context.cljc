(ns forridan.context
  (:require [clojure.spec.alpha :as s]
            forridan.interceptor))

(s/def ::queue (s/coll-of :forridan/interceptor :kind vector?))

(s/def ::stack (s/coll-of :forridan/interceptor :kind list?))

(s/def ::interceptor-key keyword?)

(s/def :forridan/context
  (s/keys :req [::queue ::stack ::interceptor-key]))
