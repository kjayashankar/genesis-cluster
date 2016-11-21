import shlex
import subprocess
import sys
print sys.argv

#python ping.py 169.254.244.97
# Tokenize the shell command
# cmd will contain  ["ping","-c1","169.254.101.84"]
#cmd=shlex.split("ping -c1 169.254.244.97")

cmd=shlex.split("ping -c1 "+sys.argv[1])

try:
   output = subprocess.check_output(cmd)
except subprocess.CalledProcessError,e:
   #Will print the command failed with its exit status
   print "The IP {0} is NotReacahble".format(cmd[-1])
else:
   print "The IP {0} is Reachable".format(cmd[-1])
