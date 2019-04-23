set -e

echo "7be85238cbbb957ab25de52b60279d40ba40d3faa72eeb2cb9fa77d6d92381e5  git-lfs-v2.7.1.tar.gz" > git-lfs-v2.7.1.checksum.txt
curl -L https://github.com/git-lfs/git-lfs/releases/download/v2.7.1/git-lfs-linux-amd64-v2.7.1.tar.gz > git-lfs-v2.7.1.tar.gz
shasum -a 256 -c git-lfs-v2.7.1.checksum.txt

cat git-lfs-v2.7.1.tar.gz | tar xz git-lfs
