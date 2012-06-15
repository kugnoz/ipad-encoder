(ns ipad-encoder.core
  (:gen-class)
  (:require [clojure.string :as str]
            [clj-commons-exec :as exec]))

(def media-dir (clojure.java.io/file "."))

(def media-exts ["smi"
                 "mkv"
                 "avi"
                 "mov"])

(defn media-filter [f]
  (let [fn (.getCanonicalPath f)]
    (some #(.endsWith fn %) media-exts)))

(defn not-in-apple-dir [f]
  (let [pd (.getParent f)]
    (if (nil? pd)
      true
      (not (.endsWith pd "/ipad")))))

(defn -main []
  (let [media-files (filter media-filter (filter not-in-apple-dir (file-seq media-dir)))]
    (loop [xs  media-files
           ctr 0]
      (if (seq xs)
        (let [path       (.getCanonicalPath (first xs))
              fn         (.getName (first xs))
              pd         (.getParent (first xs))
              apple      (clojure.java.io/file (str pd "/ipad"))
              apple-path (.getCanonicalPath apple)
              out        (clojure.java.io/file (str apple-path "/" (subs fn 0 (- (count fn ) 4)) ".m4v"))
              out-path   (.getCanonicalPath out)]
          (when-not (.exists out)
            (when-not (.exists apple) (.mkdir apple))
            (println path)
            (if (.endsWith fn ".smi")
              @(exec/sh ["cp" path (str apple-path "/" fn)])
              @(exec/sh ["arista-transcode"
                         "-d" "apple"
                         "-p" "iPad"
                         "-o" out-path
                         path])))
          (recur (rest xs) (inc ctr)))
        (println "Done:" ctr "files.")))))
