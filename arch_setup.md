# Java Development Setup Guide - Arch Linux with yay

## Prerequisites
- Arch Linux
- yay package manager

## Install yay (if needed)
```bash
sudo pacman -S --needed git base-devel
git clone https://aur.archlinux.org/yay.git
cd yay
makepkg -si
```

## Installation Commands

### Core Development Tools
```bash
# JDK (Latest)
yay -S jdk-openjdk

# JDK 17 LTS
yay -S jdk17-openjdk

# Build Tools
yay -S maven tree
```

### Environment Configuration
```bash
# Add to ~/.bashrc
export JAVA_HOME=/usr/lib/jvm/default
export PATH=$JAVA_HOME/bin:$PATH
```

## Verification Steps
```bash
# Reload shell config
source ~/.bashrc

# Check versions
java --version
javac --version
mvn --version
tree --version
```

## Managing Java Versions
```bash
# List installed versions
archlinux-java status

# Set default version
sudo archlinux-java set java-17-openjdk
```

## Project Setup Verification
```bash
cd your-project-directory
mvn clean compile
mvn test
tree src/
```

## Clean Uninstall
```bash
# Remove JDK
yay -Rns jdk-openjdk    # Latest version
yay -Rns jdk17-openjdk  # Specific version

# Remove build tools
yay -Rns maven

# Clean cache
yay -Scc

# Remove configs
rm -rf ~/.java
```

## VS Code Integration
1. Install Java Extension Pack
2. Reload VS Code
3. Wait for Java Language Server initialization

## Project Structure
```
project/
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
│       └── java/
├── pom.xml
└── .gitignore
```

## Troubleshooting
- Run `archlinux-java status` to verify active version
- Check `JAVA_HOME` with `echo $JAVA_HOME`
- Verify Maven settings with `mvn -v`

## Notes
- yay handles both official and AUR packages
- Use LTS versions for stability
- Keep build tools updated with `yay -Syu`