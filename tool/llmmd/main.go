package main

import (
	"bytes"
	"flag"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"gopkg.in/yaml.v3"
)

const (
	defaultRepoRootRel = "../.."
	profileFileName    = "profile.yaml"
	resultDirName      = "result"
)

type Profiles map[string][]string

func main() {
	profile := flag.String("profile", "", "profile name in profile.yaml (required)")
	flag.Parse()
	if *profile == "" {
		fmt.Fprintln(os.Stderr, "ERROR: -profile is required")
		os.Exit(1)
	}

	wd, _ := os.Getwd()
	toolDir := wd
	repoRoot := filepath.Clean(filepath.Join(toolDir, defaultRepoRootRel))
	profilePath := filepath.Join(toolDir, profileFileName)
	resultDir := filepath.Join(toolDir, resultDirName)
	resultFile := filepath.Join(resultDir, fmt.Sprintf("%s.md", *profile))

	b, err := os.ReadFile(profilePath)
	if err != nil {
		fmt.Fprintf(os.Stderr, "ERROR: read %s: %v\n", profilePath, err)
		os.Exit(1)
	}
	var ps Profiles
	if err := yaml.Unmarshal(b, &ps); err != nil {
		fmt.Fprintf(os.Stderr, "ERROR: parse %s: %v\n", profilePath, err)
		os.Exit(1)
	}
	paths, ok := ps[*profile]
	if !ok || len(paths) == 0 {
		fmt.Fprintf(os.Stderr, "ERROR: profile %q not found or empty\n", *profile)
		os.Exit(1)
	}

	var buf bytes.Buffer
	for _, rel := range paths {
		rel = strings.TrimSpace(rel)
		if rel == "" {
			continue
		}
		abs := filepath.Join(repoRoot, rel)
		content, err := os.ReadFile(abs)

		buf.WriteString("/" + toSlash(rel) + "\n")
		buf.WriteString("```\n")
		if err != nil {
			buf.WriteString(fmt.Sprintf("/* ERROR: %v */\n", err))
		} else {
			buf.Write(content)
			if !bytes.HasSuffix(content, []byte("\n")) {
				buf.WriteByte('\n')
			}
		}
		buf.WriteString("```\n\n")
	}

	if err := os.WriteFile(resultFile, buf.Bytes(), 0o644); err != nil {
		fmt.Fprintf(os.Stderr, "ERROR: write %s: %v\n", resultFile, err)
		os.Exit(1)
	}

	fmt.Printf("Generated: %s (profile=%s, files=%d)\n", resultFile, *profile, len(paths))
}

func toSlash(p string) string {
	return strings.ReplaceAll(p, string(filepath.Separator), "/")
}
