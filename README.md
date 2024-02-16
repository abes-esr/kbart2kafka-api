# kbart2kafka-api

Vous êtes sur le README usager. Si vous souhaitez accéder au README développement, veuillez suivre ce lien : [README-developpement](README-developpement.md)

API permettant de lire, contrôler et traiter un fichier kbart de type TSV (Tab-separated values).

## Lecture du fichier
Le fichier kbart est chargé à partir de l'application cercle-bacon ([lien vers cercles-bacon](https://cerclesbacon.abes.fr/)). Il est également possible de charger un fichier kbart via une ligne de commande directement à partir du serveur d'installation de l'API.

## Contrôle du fichier
Lors du lancement de l'API, celle-ci vérifie qu'un fichier a bien été joint. Le cas échéant, le fichier est chargé.
Après chargement du fichier, plusieurs données sont vérifiées :
- le format du nom du fichier (doit comporter obligatoirement un fournisseur, une zone/consortium, un nom de package et une date (peuvent également être adjoint deux paramètres : `_FORCE` **ou** `_BYPASS`). Exemple sur le fichier `CYBERLIBRIS_COUPERIN_SCIENCES-HUMAINES-ET-SOCIALES_2023-05-05` :
  - `CYBERLIBRIS` est le fournisseur
  - `COUPERIN` est la "Zone / consortium" (périmètre utilisateurs)
  - `SCIENCES-HUMAINES-ET-SOCIALES` est le nom du package
  - `2023-05-05` est la date du kbart
  - `_FORCE` est le paramètre qui permet de forcer le chargement malgré des erreurs dites "bloquantes"
  - `_BYPASS` est le parmètre qui permet de ne pas effectuer le calcul du bestppn dans l'API best-ppn-api ([lien github](https://github.com/abes-esr/best-ppn-api))
    > :warning: Les paramètres `_FORCE` et `_BYPASS` ne peuvent pas être utilisés ensemble.
- la présence ou non d'un paramètre _BYPASS et, le cas échéant, l'absence d'une colonne bestPpn dans le fichier
- la présence d'un provider dans le nom du fichier
- l'extension du fichier (.tsv)
- la présence de tabulations sur toutes les lignes du fichier
- la présence d'un header conforme

Le chargement antérieur d'un fichier kbart plus récent est ensuite contrôlé. Si tel est le cas, la tentative de chargement échoue.

## Traitement du fichier
Une par une, chaque ligne du fichier est lue puis envoyé dans un topic Kafka pour traitement ultérieur par une application tierce (best-ppn-api [lien github](https://github.com/abes-esr/best-ppn-api))
En cas de problème sur la lecture ou l'envoi d'une ligne du fichier kbart, un message d'erreur est envoyé sur un topic Kafka pour un traitement ultérieur par une application tierce (logskbart-api [lien github](https://github.com/abes-esr/logskbart-api))  
Une fois le fichier traité, l'API s'arrête. 
