#!/bin/bash

# Définition des variables
APP_NAME="TestFrameworkMVC"
SRC_DIR="src/main/java"
WEB_DIR="src/main/webapps"
BUILD_DIR="build"
LIB_DIR="lib"
TOMCAT_WEBAPPS="/home/mirantsoa/tomcat/apache-tomcat-10.0.16/webapps"
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"

# Nettoyage et création du répertoire temporaire
#rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/WEB-INF/classes
# Nouveau : Créer le dossier lib dans le build pour que le WAR contienne les JARs
mkdir -p $BUILD_DIR/WEB-INF/lib


# --- MODIFICATION ICI ---
# On inclut TOUS les JARs du dossier lib (servlet-api, gson, etc.) dans le classpath
# Note : Sous Linux/Mac on utilise ":" comme séparateur, "*" pour inclure tous les JARs
javac -cp "$LIB_DIR/*" -d $BUILD_DIR/WEB-INF/classes $(find $SRC_DIR -name "*.java")
# -------------------------

# Copier les fichiers web (web.xml, JSP, etc.)
cp -r $WEB_DIR/* $BUILD_DIR/

# --- IMPORTANT ---
# Il faut aussi copier les JARs (comme gson.jar) dans le dossier WEB-INF/lib du build
# sinon Tomcat ne les trouvera pas à l'exécution (même si la compilation réussit)
cp $LIB_DIR/*.jar $BUILD_DIR/WEB-INF/lib/

# Générer le fichier .war
cd $BUILD_DIR || exit
jar -cvf $APP_NAME.war *
cd ..

# Déploiement
cp -f $BUILD_DIR/$APP_NAME.war $TOMCAT_WEBAPPS/

echo "Déploiement terminé.Redémarrez Tomcat si nécessaire."