cd `dirname $0`
mkdir jacobian-host
javac src/* -d jacobian-host
echo "Main-Class: Host\n" > Manifest.txt
cd jacobian-host
jar cvfm ../jacobian-host.jar ../Manifest.txt *
cd ..
rm -r jacobian-host
rm Manifest.txt
echo "Build complete :)"
