android-birthday
================

Mes anniversaires sous android, une application toute simple.

Elle se base sur les données de vos contacts gmail et permet de gérer :
* Une liste d'exclustion de contact 
* L'heure à laquelle elle affiche sa notification

Elle est éteinte le reste du temps et n'utilise ni mémoire, ni CPU (AlarmManager). Elle n'a pas accès à l'internet (vos données personnelles restent chez vous [et chez google]). 


Nécessite au moins android 4. Pour une version antérieur, il suffit de modifier le manifest pour qu'il accepte les version précedentes et de remplacer les `android:theme="..."` toujours dans le manifest par un quelque chose disponible sur une précédente version d'android.
