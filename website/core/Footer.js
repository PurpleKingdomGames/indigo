/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

class Footer extends React.Component {
  docUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    const docsUrl = this.props.config.docsUrl;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    return `${baseUrl}${docsPart}${langPart}${doc}`;
  }

  pageUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    return baseUrl + (language ? `${language}/` : '') + doc;
  }

  render() {
    return (
      <footer className="nav-footer" id="footer">
        <section className="sitemap">
          <a href={this.props.config.baseUrl} className="nav-home">
            {this.props.config.footerIcon && (
              <img
                src={this.props.config.baseUrl + this.props.config.footerIcon}
                alt={this.props.config.title}
                width="66"
                height="58"
              />
            )}
          </a>
          <div>
            <h5>Getting Started</h5>
            <a href={this.docUrl('quickstart/setup-and-configuration', this.props.language)}>
              Setup & Configuration Guide
            </a>
            <a href={this.docUrl('quickstart/hello-indigo', this.props.language)}>
              "Hello, Indigo!" Tutorial
            </a>
            <a href={this.docUrl('quickstart/examples', this.props.language)}>
              Links to examples
            </a>
            <a href={this.docUrl('quickstart/project-templates', this.props.language)}>
              Project templates
            </a>
          </div>
          <div>
            <h5>Community</h5>
            {/* <a href={this.pageUrl('users.html', this.props.language)}>
              User Showcase
            </a>
            <a
              href="https://stackoverflow.com/questions/tagged/"
              target="_blank"
              rel="noreferrer noopener">
              Stack Overflow
            </a> */}
            <a href="https://discord.gg/b5CD47g">Discord</a>
            <a href="https://github.com/PurpleKingdomGames/indigo/discussions">GitHub Discussons</a>
            <a
              href="https://twitter.com/indigoengine"
              target="_blank"
              rel="noreferrer noopener">
              Twitter
            </a>
          </div>
          <div>
            <h5>Sponsor us!</h5>
            {/* <a href={`${this.props.config.baseUrl}blog`}>Blog</a> */}
            {/* <a href="https://github.com/PurpleKingdomGames/indigo">GitHub</a>
            <a
              className="github-button"
              href={this.props.config.repoUrl}
              data-icon="octicon-star"
              data-count-href="/facebook/docusaurus/stargazers"
              data-show-count="true"
              data-count-aria-label="# stargazers on GitHub"
              aria-label="Star this project on GitHub">
              Star
            </a>
            <br />
            <br /> */}
            <a href="https://github.com/sponsors/PurpleKingdomGames">GitHub Sponsors</a>
            <a href="https://www.patreon.com/indigoengine">Patreon</a>
            {this.props.config.twitterUsername && (
              <div className="social">
                <a
                  href={`https://twitter.com/${this.props.config.twitterUsername}`}
                  className="twitter-follow-button">
                  Follow @{this.props.config.twitterUsername}
                </a>
              </div>
            )}
            {this.props.config.facebookAppId && (
              <div className="social">
                <div
                  className="fb-like"
                  data-href={this.props.config.url}
                  data-colorscheme="dark"
                  data-layout="standard"
                  data-share="true"
                  data-width="225"
                  data-show-faces="false"
                />
              </div>
            )}
          </div>
        </section>
        
        {/* <section className="copyright">{this.props.config.copyright}</section> */}
      </footer>
    );
  }
}

module.exports = Footer;
