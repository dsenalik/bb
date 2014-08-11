#!/usr/bin/perl
use strict;
use warnings;
# configuration
my @bins = ( "/usr/local/bin",    # development unreleased versions
             "/usr/local/bb" );   # programs on git repository
my $indent = "   ";

# example of how information should look in each program file
=bb
(This program) Lists all bb (black-box) programs
Type the program name followed by --help for more information
The first parameter acts like a grep, e.g. type bb 454
for only a list of programs with 454 in the name
=cut bb
# if it is a shell script, use #=bb and #=cut bb instead

# get a alphabetically sorted list of bb. programs, ignoring the directory
my @bb;
for my $bin ( @bins )
  { push ( @bb, glob ( "$bin/bb.*" ) ); }
@bb = sort ( { ($a=~m|([^/]*)$|)[0] cmp ($b=~m|([^/]*)$|)[0] } @bb );

# get command line "grep" parametr
my $filter = $ARGV[0];
my $filtermatched = 0;

# print the list
foreach my $abb ( @bb )
  {
    # grep filter
    next if ( ( $filter ) and ( $abb !~ m/$filter/ ) );
    $filtermatched++;

    # read inside program for "=bb" text
    my $info = "";
    my $pod = 0;
    my $shellscript = 0;
    open my $FILE,"<", $abb or die "Error opening file \"$abb\" for input: $!\n";
    while ( my $aline = <$FILE> )
      {
        if ( $aline =~ m/^#?=cut/ ) { last; }
        if ( $pod ) 
          {
            if ( $shellscript )  # shell script must prefix every line with "#" character
              { $aline =~ s/^\s*#//; }
            $info .= $indent . $aline;
          }
        if ( $aline =~ m/^(#?)=bb/ )
          {
            $shellscript = $1;
            $pod = 1;
          }
      } # while
    close $FILE;
    
    # print program name and description if one was found
    ( my $namenopath = $abb ) =~ s|^.*/||;
    print "$namenopath\n", $info;

  } # foreach @bb

# grep filtered out everything
unless ( $filtermatched )
  { print "Your filter word \"$filter\" did not match any programs\n"; }

print "\n";
exit 0;
