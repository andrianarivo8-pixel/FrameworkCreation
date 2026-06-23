#!/bin/bash

# Définition des variables
APP_NAME="FrameworkMVC"
SRC_DIR="src/main/java"
# WEB_DIR="src/main/webapps"
BUILD_DIR="build_jar"
LIB_DIR="lib"
TOMCAT_WEBAPPS="/home/mirantsoa/tomcat/apache-tomcat-10.0.16/webapps"
TARGET_PROJECT_LIB="../testFramework/lib"

# SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"


# Nettoyage et création du répertoire temporaire
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

echo "💡 Compilation du framework..."

# Compilation des fichiers Java avec le JAR des Servlets
# On inclut les dépendances existantes si tu en as dans le dossier lib/ de SysFramework
if [ -d "$LIB_DIR" ] && [ "$(ls -A $LIB_DIR)" ]; then
    javac -cp "$LIB_DIR/*" -d $BUILD_DIR $(find $SRC_DIR -name "*.java")
else
    javac -d $BUILD_DIR $(find $SRC_DIR -name "*.java")
fi

# Vérification du succès de la compilation
if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation. Génération du JAR annulée."
    exit 1
fi

echo "📦 Création du fichier .jar..."

# Générer le fichier .jar à partir des .class compilés
cd $BUILD_DIR || exit
jar -cvf $APP_NAME.jar *
cd ..

# Déploiement vers l'autre projet
if [ -d "$TARGET_PROJECT_LIB" ]; then
    echo "🚚 Copie de $APP_NAME.jar dans le classpath de l'autre projet..."
    cp -f $BUILD_DIR/$APP_NAME.jar "$TARGET_PROJECT_LIB/"
    echo "✅ Déploiement et mise à jour du JAR terminés avec succès.Redémarrez Tomcat si nécessaire."
else
    echo "⚠️  Le dossier de destination '$TARGET_PROJECT_LIB' n'existe pas."
    echo "Le fichier JAR est disponible ici : $BUILD_DIR/$APP_NAME.jar"
fi

TOMCAT_BIN="/home/mirantsoa/tomcat/apache-tomcat-10.0.16/bin"

# echo "🚀 Démarrage de Tomcat..."
# cd "$TOMCAT_BIN" || exit
# pgrep -f "org.apache.catalina.startup.Bootstrap" > /dev/null 2>&1 && ./shutdown.sh
# ./startup.sh