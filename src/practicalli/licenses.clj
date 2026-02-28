(ns practicalli.licenses
  "Light wrapper around the [clj-spdx](https://github.com/pmonks/clj-spdx)
  library to access the license repository managed by [SPDX](https://spdx.dev/)."
  (:require [clojure.string :as str]
            [spdx.licenses :as sl]))

;; Until the SPDX library offers a cache that is built as needed, the use of
;; licenses info included in the SPDX JAR should be fit for purpose because
;; licenses don't change very often.
(when (str/blank? (System/getProperty "org.spdx.useJARLicenseInfoOnly"))
  (System/setProperty "org.spdx.useJARLicenseInfoOnly" (str true)))
;; Without the above the following would load all licenses from SPDX web API.
;; Local cache is stored in the user cache directory (i.e.
;; `${XDG_CACHE_HOME}/Spdx-Java-Library` or `${HOME}/.cache/Spdx-Java-Library`)
(sl/init!)

(defn id->license
  "Retrieve from [SPDX](https://spdx.dev/) library the full info and text of
  a license identified with the given string `id`, or `nil` if not found.
  If `id` is not provided it defaults to `\"EPL-1.0\"`, a license typically
  used in clojure projects.
  Other popular licenses are `\"MIT\"`, `\"Apache-2.0\"`, `\"EPL-2.0\"`, or any
  identifier found in the [SPDX license list](https://spdx.org/licenses/).
  Returns a map with the following entries:
    - `:license/id`, the identifier given as argument
    - `:license/name`, the name of the license
    - `:license/url`, the URL associated with the license
    - `:license/text`, the full text of the license
  "
  ([] (id->license nil))
  ([id]
   (let [id (if (str/blank? (str id)) "EPL-1.0" (str id))
         {:keys [id name text see-also]}
         (sl/id->info id {:include-large-text-values? true})
         missing (fn [fieldname]
                   (format "*(No %s for %s in SPDX)*" fieldname id))]
     (when id
       {:license/id   id
        :license/name (or name             (missing "name"))
        :license/url  (or (first see-also) (missing "URL"))
        :license/text (or text             (missing "text"))}))))
