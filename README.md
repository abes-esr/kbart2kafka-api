# kbart2kafka-api

Vous êtes sur le README usager. Si vous souhaitez accéder au README développement, veuillez suivre ce lien : [README-developpement](README-developpement.md)

API permettant de lire, contrôler et traiter un fichier kbart de type TSV (Tab-separated values).

## Lecture du fichier
Le fichier kbart est chargé à partir de l'application cercle-bacon ([lien vers cercles-bacon](https://cerclesbacon.abes.fr/)). Il est également possible de charger un fichier kbart via une ligne de commande directement à partir du serveur d'installation de l'API.

## Contrôle du fichier
Lors du lancement de l'API, celle-ci vérifie qu'un fichier a bien été joint. Le cas échéant, le fichier est chargé.
Après chargement du fichier, plusieurs données sont vérifiées :
- le format du nom du fichier
- la présence ou non d'un paramètre _BYPASS et, le cas échéant, l'absence d'une colonne bestPpn dans le fichier
- la présence d'un provider dans le nom du fichier
- l'extension du fichier (.tsv)
- la présence de tabulations sur toutes les lignes du fichier
- la présence d'un header conforme

Le chargement antérieur d'un fichier kbart plus récent est ensuite contrôlé. Si tel est le cas, la tentative de chargement échoue.

## Traitement du fichier
Une par une, chaque ligne du fichier est lue puis envoyé dans un topic Kafka pour traitement ultérieur par une application tierce (best-ppn-api : [lien vers la page github du projet best-ppn-api](https://github.com/abes-esr/best-ppn-api) )
En cas de problème sur la lecture ou l'envoi d'une ligne du fichier kbart, un message d'erreur est envoyé sur un topic Kafka pour un traitement ultérieur par une application tierce (logskbart-api : [lien vers la page github du projet logskbart-api](https://github.com/abes-esr/logskbart-api) )  
Une fois le fichier traité, l'API s'arrête. 
