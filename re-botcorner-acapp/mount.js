const mountApp = id => {
	const Nq=kX(dAe);Nq.use(TX());Nq.mount(id);
};
export{iw as m,KU as t};

export class Acapp {
	constructor(id, AcWingOS) {
		id = `#${id}`;
		mountApp(id, AcWingOS);
	}
}

mountApp(`#app`, "AcWingOS");