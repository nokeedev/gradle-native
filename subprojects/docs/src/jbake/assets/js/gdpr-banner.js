class GDPRBanner extends HTMLElement {
	constructor() {
		super();
	}

	connectedCallback() {
		if (window.localStorage.getItem("gdpr-dismissed") === null) {
			this.innerHTML = `
				<style>
					#gdpr-banner {
						/*display: none;*/
					}
					#gdpr-banner-content {
						background: white;
						position: fixed;
						bottom: 0;
						left: 0;
						width: 100%;
						display: flex;
						padding: 0.5em;
					}

					#gdpr-banner-content-text {
						/*background: #00bf00;*/

						display: inline-block;
						margin-top: auto;
						margin-bottom: auto;
					}

					#gdpr-banner-content-button {
						/*background: #00fafa;*/

						display: inline-block;
						min-width: 5em;
						text-align: center;
						margin: auto;
					}
				</style>

				<div id="gdpr-banner">
					<div id="gdpr-banner-content">
						<div id="gdpr-banner-content-text">This website uses cookies and other technology to provide you a more personalized experience.</div>
						<div id="gdpr-banner-content-button"><a href="">Dismiss</a></div>
					</div>
				</div>
			`;
			const button = this.querySelector("a");
			button.addEventListener("click", () => this.handleClick());
		}
	}

	handleClick() {
		window.localStorage.setItem("gdpr-dismissed", 1);
		this.querySelector("#gdpr-banner").style.display = "none";
	}
}
customElements.define("gdpr-banner", GDPRBanner);
