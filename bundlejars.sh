export JARS=`ls jars`
for JAR in $JARS; do 
    unzip -o jars/$JAR -d tmp
done
cd tmp; zip -r $1 *
